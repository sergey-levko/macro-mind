## Why

The saved weekly insight card displays a raw ISO date string (e.g. "Week of 2026-05-19") instead of the human-readable range already computed by `formatWeekLabel` (e.g. "May 19 – 25"). The helper exists and is used elsewhere in the same component — the card just isn't calling it.

## What Changes

- In `InsightPanel` (`Coach.tsx` line 255), replace the raw `Week of ${insight.periodStart}` interpolation with a call to `formatWeekLabel(insight.periodStart)` for the `WEEKLY` type

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `coach-insights`: The saved weekly insight card's date label now renders a formatted week range instead of a raw ISO date string

## Impact

**Modified files:**
- `frontend/src/pages/Coach.tsx` — one-line change in `InsightPanel`

**Database tables:** none

**API endpoints:** none

**Non-goals:**
- Formatting the daily insight's `periodStart` label (separate concern)
- Changing how `periodStart` is stored or returned by the backend
