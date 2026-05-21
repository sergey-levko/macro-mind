## Context

The Meal Log page currently hardcodes today's date via `todayIso()`. The backend already supports arbitrary dates — `GET /api/v1/meal-logs?date=<ISO>` accepts any date and `POST /api/v1/meal-logs` body includes `loggedAt`. This is a pure frontend change.

## Goals / Non-Goals

**Goals:**
- Replace the hardcoded today-only date with navigable date state
- Prev/next day buttons and a readable date label in the page header
- Meals added on a non-today date use that date's midnight UTC as `loggedAt`

**Non-Goals:**
- Date picker widget (arrow navigation is sufficient)
- Future dates (cap navigation at today)
- Backend changes

## Decisions

**Date state lives in the `MealLog` page component, not in a context or URL param.**
- Rationale: Only `MealLog` uses it; no deep-linking requirement was stated. A URL param (`/meal-log?date=2026-05-20`) would be cleaner for bookmarking but adds routing complexity not warranted here.
- Alternative considered: URL search param via `useSearchParams` — deferred as a future enhancement.

**`loggedAt` is set to the selected date at local midnight expressed as UTC ISO string.**
- Rationale: Consistent with how the backend parses and groups logs by date (UTC day boundaries).
- Alternative: Send the date string only and let the backend default — rejected because the backend currently accepts an explicit `loggedAt`.

**Navigation is capped at today (no future dates).**
- The "next" button is disabled when the selected date equals today's ISO date.

## Risks / Trade-offs

- [Timezone mismatch] A user in UTC+10 logging at 11pm will see a different "today" than UTC. → Acceptable for MVP; timezone support is a future concern.
- [No URL persistence] Navigating away and back resets to today. → Consistent with current behaviour; low friction for target use case.
