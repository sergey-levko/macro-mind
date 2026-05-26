## Context

Generating AI insights in `AiAdviceService.generateAdvice()` currently issues 100+ SQL round-trips before the LLM call fires, and holds an open DB connection for the entire LLM response window (5–30 s). The root causes are in `AdvicePromptBuilder.buildUserPrompt()`: a lazy `@OneToMany` on `MealLog.items` causes one SELECT per log row, and a `foodRepository.findById()` inside the inner loop causes one SELECT per meal item. Additionally, `AiAdviceService` is annotated `@Transactional` at class level, pinning a connection-pool slot across the AI call.

A secondary issue is the lack of an idempotency guard: re-requesting the same `(adviceType, periodStart)` pair re-fires the LLM and inserts a duplicate `ai_advice` row.

## Goals / Non-Goals

**Goals:**
- Reduce SQL round-trips for advice generation from O(M × N) to O(1) for the meal data load phase
- Release the DB connection before the AI call starts
- Prevent duplicate advice records for the same `(userId, adviceType, periodStart)` triple

**Non-Goals:**
- Async/reactive AI calls
- Caching AI responses in Redis or in-memory (idempotency guard covers the duplicates case)
- Configuring timeouts or retries on `ChatClient`
- Fixing the minor double-SELECT in `NutritionalGoalService.setGoal()`

## Decisions

### 1. Fix MealLog.items N+1 with JOIN FETCH

**Decision**: Add a `@Query` to `MealLogRepository` that uses `LEFT JOIN FETCH m.items` to load meal items eagerly in the same query.

**Alternatives considered**:
- `@EntityGraph` — equivalent result but less explicit about the join strategy; JPQL `JOIN FETCH` is more readable and easier to test.
- `FetchType.EAGER` on the mapping — would affect all queries on `MealLog`, not just this one; unacceptable blast radius.

### 2. Fix food N+1 with bulk fetch

**Decision**: Collect all distinct `foodId`s from the loaded items into a `Set<UUID>`, call `foodRepository.findAllById(ids)` once, and build an in-memory `Map<UUID, Food>` for the loop.

**Alternatives considered**:
- JPQL IN-clause query — `findAllById` already generates an `IN (...)` query under Spring Data JPA, so no custom query is needed.
- Join food into the meal log query — would produce a wide Cartesian result; keeping it as a second targeted query is cleaner.

### 3. Narrow transaction scope in AiAdviceService

**Decision**: Remove `@Transactional` from the class. Annotate two private helper methods instead: `readAdviceData()` (reads user, goal, meal logs) and `saveAdvice()` (inserts the `ai_advice` row). The AI call happens between them without a transaction.

**Alternatives considered**:
- `@Transactional(propagation = NEVER)` on the method — still no connection released before the AI call.
- Moving the AI call to a separate `@Async` method — requires reactive or thread-pool plumbing; out of scope.

### 4. Idempotency guard

**Decision**: At the start of `generateAdvice()`, call `adviceRepository.findByUserIdAndAdviceTypeAndPeriodStart(userId, type, start)`. If a record exists and `preview == false`, return it immediately (HTTP 200). If `preview == true`, skip the guard and always call the LLM (preview responses are never persisted and callers expect a fresh generation).

**Alternatives considered**:
- Return HTTP 409 Conflict — less useful for clients; returning the existing record is idempotent and avoids client-side retry logic.
- Unique constraint on `(user_id, advice_type, period_start)` — would enforce at DB level but requires a Liquibase changeset and changes the error surface. Preferred to avoid the schema change since the application-level guard is sufficient.

## Risks / Trade-offs

- **Stale advice on duplicate request**: The idempotency guard returns the first-ever advice record for a period, even if the user has added more meal logs since. → Acceptable for the initial fix; a "regenerate" flag can be added later if needed.
- **Large IN clause for food bulk fetch**: If a weekly period has thousands of items, the `IN (...)` clause could be large. → In practice, a week of meals yields at most a few hundred distinct foods; PostgreSQL handles this comfortably.
- **JOIN FETCH with pagination**: Adding `JOIN FETCH` to a collection can interfere with Hibernate's in-memory pagination warning. → The meal-log query does not use pagination, so this is not a concern here.

## Migration Plan

- No schema changes; no Liquibase changeset.
- Existing `ai_advice` rows are unaffected.
- Duplicate rows already in the DB are not cleaned up (out of scope).
- Deploy is a straight drop-in replacement with no rollback concerns beyond reverting the code.
