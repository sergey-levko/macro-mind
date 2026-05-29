## Why

Meal templates now provide a more flexible and reusable alternative to the one-shot "copy previous day" feature — users can save named meal sets and apply them to any date. The copy-yesterday shortcut adds UI complexity and a dedicated API endpoint without offering anything templates cannot already do. Removing it simplifies both the frontend and backend.

## What Changes

- Remove the "Copy previous day" button from the Meal Log page header
- Remove `POST /api/v1/meal-logs/copy-previous-day` endpoint
- Remove the backend service method and controller action that implement the copy logic
- Remove associated frontend hook / API call

## Capabilities

### New Capabilities

*(none — this is a removal-only change)*

### Modified Capabilities

- `copy-previous-day-meals`: Capability removed entirely; no replacement within this spec — users are directed to the meal templates feature instead
- `frontend-meal-logging`: "Copy previous day" button and its loading/disabled state logic removed from the Meal Log page header

## Non-goals

- No changes to the meal templates feature
- No data migration — existing meal logs are unaffected
- No deprecation period or feature flag; the endpoint is removed directly

## Impact

- **Deleted API endpoint**: `POST /api/v1/meal-logs/copy-previous-day`
- **Backend**: remove `copyPreviousDay` method from `MealLogService` and the corresponding controller action; remove related DTO if used only by this endpoint
- **Frontend**: remove "Copy previous day" button, its `onClick` handler, and the `copyPreviousDayMeals` API call from `MealLog.tsx`
- **Tests**: delete unit and integration tests that cover the copy-previous-day endpoint/service method
- **Database tables affected**: none — no schema changes required
