## Why

Users who eat similar meals day to day currently re-log everything from scratch each morning. A single "Copy previous day" button eliminates repetitive entry by duplicating the prior day's meal logs as an editable starting point.

## What Changes

- New API endpoint `POST /api/v1/meal-logs/copy-previous-day` accepts a target date, finds all meal logs from the day before, and creates identical meal logs + items on the target date
- The meal log page shows a "Copy previous day" button in the header; it is disabled when there are no meals on the preceding day
- After a successful copy the page refreshes to show the newly created logs

## Capabilities

### New Capabilities
- `copy-previous-day-meals`: Backend endpoint that bulk-copies all meal logs and items from `targetDate - 1` day to `targetDate` for the authenticated user

### Modified Capabilities
- `frontend-meal-logging`: Add a "Copy previous day" button to the meal log page header; disabled when the source day has no logs

## Impact

**Database tables:** `meal_logs`, `meal_items` (inserts only — no schema changes)

**New API endpoints:**
- `POST /api/v1/meal-logs/copy-previous-day` — body `{ "date": "YYYY-MM-DD" }`, returns list of created `MealLogSummary`; returns empty list if source day has no meals

**Modified files:**
- Backend meal slice: new controller endpoint + service method
- `frontend/src/pages/MealLog.tsx` — add "Copy previous day" button

**Non-goals:**
- Copying from an arbitrary source date (always previous day only)
- Deduplication — if the target date already has meals, copies are added on top (user can delete unwanted logs)
- Editing the copy before confirming — this is a one-shot duplicate, not a wizard
- Adjusting timestamps to match the target date's time-of-day (logs are created at midnight UTC of the target date)
