## 1. Backend — Remove copy-previous-day endpoint and service logic

- [x] 1.1 Delete the `copyPreviousDay` controller action from `MealLogController` and its route mapping (`POST /api/v1/meal-logs/copy-previous-day`); delete the `CopyPreviousDayRequest` DTO if it is used only by this endpoint; remove any unused imports. Commit: `chore: remove copy-previous-day controller and DTO`
- [x] 1.2 Delete the `copyPreviousDay` service method from `MealLogService` (and any private helpers it calls); remove any unused imports. Commit: `chore: remove copy-previous-day service method`

## 2. Backend — Delete tests

- [x] 2.1 Delete all unit test cases in `MealLogServiceTest` (or equivalent) that cover the `copyPreviousDay` method. Commit: `test: remove copy-previous-day service unit tests`
- [x] 2.2 Delete all integration test cases in `MealLogControllerTest` / `MealLogIntegrationTest` that cover `POST /api/v1/meal-logs/copy-previous-day`. Commit: `test: remove copy-previous-day integration tests`

## 3. Frontend — Remove UI button and API call

- [x] 3.1 Remove the `copyPreviousDayMeals` API call (and its type/helper) from `api.ts` or the relevant API module; remove unused imports. Commit: `chore: remove copy-previous-day API call from frontend`
- [x] 3.2 Remove the "Copy previous day" button element, its `onClick` handler, and all associated loading/disabled state (`isCopying`, `hasPreviousDayMeals`, etc.) from `MealLog.tsx`; remove any `useEffect` or query that was fetching the previous day's data solely to drive the button's disabled state. Confirm no dead imports remain. Commit: `chore: remove copy-previous-day button from MealLog page`
