## Why

Generating AI insights is slow because `AdvicePromptBuilder` issues 100+ SQL round-trips (N+1 on `meal_items` collection load + per-item `foodRepository.findById()` inside a loop) before the AI call even fires. A second issue is that the entire AI call runs inside an open database transaction, pinning a connection-pool slot for the duration of the LLM response (5–30 s). Additionally, there is no deduplication guard: re-requesting the same `(adviceType, periodStart)` pair fires a fresh LLM call and persists a duplicate record.

## What Changes

- **Fix N+1 in meal log loading**: Replace the plain derived query with a `JOIN FETCH m.items` JPQL query so all `MealItem` rows are loaded in a single SQL JOIN rather than one lazy SELECT per log.
- **Fix N+1 in food loading**: Replace the per-item `foodRepository.findById()` call inside the inner loop with a single `foodRepository.findAllById(ids)` bulk fetch before the loop.
- **Narrow transaction scope**: Remove the class-level `@Transactional` from `AiAdviceService` and split `generateAdvice()` into a read phase (transactional), an AI call (no transaction), and a write phase (transactional). This releases the DB connection before the LLM call.
- **Add idempotency guard**: Before calling the LLM, check whether an advice record already exists for `(userId, adviceType, periodStart)`. If one exists and `preview == false`, return it immediately without a second AI call.

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

- `ai-advice`: Generating advice for a `(adviceType, periodStart)` pair that already has a saved record now returns the existing record (HTTP 200) instead of creating a duplicate. This makes the endpoint idempotent for non-preview requests.

## Impact

- **Database**: No schema changes. No Liquibase changeset needed.
- **API**: `POST /api/v1/advice` behavior changes for duplicate non-preview requests (returns existing record rather than a new one).
- **Backend files**: `AdvicePromptBuilder.java`, `AiAdviceService.java`, `MealLogRepository.java`, `AiAdviceController.java` (response status for duplicate).
- **Tests**: `AiAdviceIntegrationTest.java` — update duplicate-request test to assert 200 + same record ID; add N+1 regression test asserting query count.

## Non-goals

- Async/non-blocking AI calls (would require reactive stack changes).
- Response caching (Redis or in-memory) — idempotency guard covers the duplicate-call case.
- Timeout or retry configuration on `ChatClient` — separate concern.
- Fixing the minor double-SELECT in `NutritionalGoalService.setGoal()`.
