## Why

Users can only view and log meals for today, making it impossible to correct forgotten entries or log a meal from yesterday. Allowing meal logging for any past date makes the app genuinely useful for real tracking habits.

## What Changes

- Add date navigation to the Meal Log page (back/forward by day, date picker)
- `GET /api/v1/meal-logs?date=<date>` already accepts any ISO date — no backend query change needed
- `POST /api/v1/meal-logs` body already accepts `loggedAt` — ensure the selected date is sent
- Frontend: replace hardcoded `todayIso()` with a navigable date state

## Non-goals

- Editing meal logs from future dates
- Bulk import or copy-paste from one day to another
- Calendar heatmap or historical analytics view

## Capabilities

### New Capabilities

- `meal-log-history`: Date navigation on the Meal Log page — browse any past date, add/delete meals and items for that date

### Modified Capabilities

- `frontend-meal-logging`: The meal logging page gains date navigation; the requirement for loading today's meals becomes "loading the selected date's meals"

## Impact

- **Frontend**: `src/pages/MealLog.tsx` — replace `todayIso()` constant with navigable date state; add prev/next day buttons and a date display in the page header
- **Backend**: No changes — `GET /api/v1/meal-logs?date=` and `POST /api/v1/meal-logs` with `loggedAt` already support arbitrary dates
- **API endpoints**: No additions or modifications
- **Database tables**: None affected
