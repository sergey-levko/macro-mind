## 1. Backend — Service Method

- [x] 1.1 Add `copyPreviousDay(UUID userId, LocalDate targetDate)` to `MealService`: compute `sourceDate = targetDate.minusDays(1)`, reuse the existing `findByUserIdAndLoggedAtGreaterThanEqualAndLoggedAtLessThan` range query, for each source `MealLog` create a new `MealLog` with `loggedAt = targetDate.atStartOfDay(ZoneOffset.UTC).toInstant()` and the same `mealType`, copy each `MealItem` (same `foodId`, `quantityG`), save and return `List<MealLogSummaryResponse>` (empty list when source has no logs)

## 2. Backend — Request DTO and Controller

- [x] 2.1 Create `CopyMealsRequest` record with `@NotNull LocalDate date` field
- [x] 2.2 Add `POST /api/v1/meal-logs/copy-previous-day` to `MealController`: accept `@Valid @RequestBody CopyMealsRequest`, return `List<MealLogSummaryResponse>` from `mealService.copyPreviousDay(userId, request.date())`

## 3. Backend — Tests

- [x] 3.1 Add integration test `copyPreviousDay_copiesAllLogsAndItems`: create two meal logs with items on day 1; call `POST /api/v1/meal-logs/copy-previous-day` with day 2; assert response contains 2 entries; assert `GET /api/v1/meal-logs?date=<day2>` returns 2 logs with the same food items
- [x] 3.2 Add integration test `copyPreviousDay_noSourceMeals_returnsEmpty`: call copy for a date with no previous-day logs; assert HTTP 200 with empty array `[]`

## 4. Frontend — Copy Button

- [x] 4.1 In `MealLog.tsx`, add `copying` boolean state and `copyMessage` string state; add a "Copy previous day" button in the page header that `POST`s to `/api/v1/meal-logs/copy-previous-day` with `{ date: selectedDate }`, then calls `loadLogs()`; disable the button while `copying` is true
- [x] 4.2 After a successful copy response: if the returned array is empty set `copyMessage` to `"No meals on previous day"` (auto-clear after 3 s); otherwise clear any existing `copyMessage` so the refreshed logs speak for themselves; render `copyMessage` as a small inline note near the button
