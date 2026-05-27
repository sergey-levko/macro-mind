## Why

The Dashboard always shows today's data, making it impossible to review past macro performance without switching to the Meal Log page. Adding date navigation lets users compare any day's intake against their targets directly from the dashboard.

## What Changes

- Add Previous/Next day buttons and a date label to the Dashboard header, capped at today (no future navigation)
- The daily macro summary card fetches `GET /api/v1/dashboard/daily?date=<selected-date>` instead of always using today
- The weekly bar chart always shows the week containing the selected date, updating as the date changes
- No new API endpoints — the backend `GET /api/v1/dashboard/daily?date=` and `GET /api/v1/dashboard/weekly?weekStart=` already accept arbitrary dates

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `frontend-dashboard`: Add date navigation controls to the dashboard; summary card and weekly chart respond to the selected date instead of always showing today.

## Impact

- **Frontend only** — `Dashboard.tsx` gains `selectedDate` state and date navigator component
- No backend changes required
- Affected tables (read-only): `meal_items`, `meal_logs`, `nutritional_goals`
- Affected API endpoints (existing, no changes): `GET /api/v1/dashboard/daily`, `GET /api/v1/dashboard/weekly`, `GET /api/v1/nutritional-goals`

## Non-goals

- Weekly chart navigation is not added; the chart always shows the week that contains `selectedDate`
- No date-range picker or calendar popover — Previous/Next buttons only
- No persistence of the selected date across page reloads
