## 1. Fix N+1 — Meal Log Repository

- [x] 1.1 Add a `@Query` to `MealLogRepository` replacing the plain derived finder with `SELECT m FROM MealLog m LEFT JOIN FETCH m.items WHERE m.userId = :userId AND m.loggedAt >= :start AND m.loggedAt < :end`; update `AdvicePromptBuilder` to call the new method by name

## 2. Fix N+1 — Food Bulk Fetch

- [x] 2.1 In `AdvicePromptBuilder.buildUserPrompt()`, collect all distinct `foodId`s from the loaded items into a `Set<UUID>`, call `foodRepository.findAllById(ids)` once, build an in-memory `Map<UUID, Food>`, and replace the per-item `findById` call with a map lookup

## 3. Narrow Transaction Scope

- [x] 3.1 Remove `@Transactional` from `AiAdviceService` class level; extract a `@Transactional` `readContext()` helper that loads user + goal + meal-log data and returns a value object, and a `@Transactional` `saveAdvice()` helper that persists the record; the main `generateAdvice()` method calls them sequentially with the AI call in between (no open transaction during the LLM call)

## 4. Idempotency Guard

- [x] 4.1 At the start of `generateAdvice()`, when `preview == false`, call `adviceRepository.findByUserIdAndAdviceTypeAndPeriodStart(...)` and return the existing `AiAdviceResponse` (HTTP 200) immediately if a record is found; add the corresponding unit test covering the short-circuit path and the preview bypass path
- [ ] 4.2 Update `AiAdviceController` to return `ResponseEntity<AiAdviceResponse>` so it can return 201 for new records and 200 for existing ones; update the existing integration tests in `AiAdviceIntegrationTest` to assert 201 on first call and 200 on the duplicate call with the same record `id`
