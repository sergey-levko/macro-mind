## Context

The Dashboard page currently hardcodes `today` as the date for both the daily macro summary card (`GET /api/v1/dashboard/daily?date=<today>`) and the weekly bar chart (`GET /api/v1/dashboard/weekly?weekStart=<monday>`). The backend already supports arbitrary dates on both endpoints. This change is purely frontend.

The Meal Log page (`MealLog.tsx`) already implements the same date-navigation pattern: a `selectedDate` state string, Previous/Next buttons, a formatted date label, and a "Today" shortcut. The dashboard navigator should match this pattern exactly for UX consistency.

## Goals / Non-Goals

**Goals:**
- Add `selectedDate` state (defaults to today) to `Dashboard.tsx`
- Render a date navigator row (Previous button, date label, Next button, Today shortcut) above the summary card
- Disable Next when `selectedDate === todayStr()`
- Pass `selectedDate` to both the daily summary fetch and the weekly chart fetch (derive `weekStart` from `selectedDate`)
- Re-fetch both endpoints whenever `selectedDate` changes

**Non-Goals:**
- No calendar/date-picker popover — Previous/Next buttons only
- No persistence of `selectedDate` across reloads
- No separate week navigator for the bar chart; the chart always shows the week containing `selectedDate`
- No backend changes

## Decisions

**Derive `weekStart` from `selectedDate` on the frontend**
The weekly chart should show the week containing the selected date, not always the current week. `weekStart` = Monday of the week containing `selectedDate`, computed with the same `mondayStr()` helper already used in `MealLog.tsx` but parameterised on `selectedDate`. This keeps a single date as the source of truth.

**Reuse the existing navigator component shape from MealLog**
Rather than a new abstraction, inline the same Previous/Next/label pattern directly in Dashboard.tsx. It's 3 UI elements and doesn't justify a shared component at this stage. If a third page needs it, extract then.

## Risks / Trade-offs

- **Double fetch on mount** — `selectedDate` initialises to today and both effects fire once on mount. This is the same behaviour as Meal Log and is acceptable.
- **Weekly chart jumps week on navigation** — when the user crosses a Monday boundary (e.g. navigates from Tuesday to Sunday), the weekly chart data changes significantly. This is expected and desirable behaviour.
