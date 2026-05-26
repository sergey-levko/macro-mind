## Context

The `InsightPanel` component in `Coach.tsx` shows a date label above saved insight content. For weekly insights it currently renders `` `Week of ${insight.periodStart}` `` where `periodStart` is a raw ISO date (e.g. `2026-05-19`). The `formatWeekLabel` helper already exists in the same file and produces human-readable ranges like `"May 19 – 25"` or `"This week (May 19 – 25)"` — it just isn't being called here.

## Goals / Non-Goals

**Goals:**
- Replace the raw ISO date in the weekly insight card's date label with the output of `formatWeekLabel`

**Non-Goals:**
- Reformatting the daily insight's `periodStart` label
- Any backend or API changes

## Decisions

### Single call-site change

Replace line 255 in `Coach.tsx`:
```tsx
// before
{type === 'WEEKLY' ? `Week of ${insight.periodStart}` : insight.periodStart}

// after
{type === 'WEEKLY' ? formatWeekLabel(insight.periodStart) : insight.periodStart}
```

`formatWeekLabel` already handles the "This week" prefix for the current week and the cross-month formatting — no additional logic is needed.

## Risks / Trade-offs

None — `formatWeekLabel` is already used on the same page and `insight.periodStart` is always the ISO Monday date of the insight's week.
