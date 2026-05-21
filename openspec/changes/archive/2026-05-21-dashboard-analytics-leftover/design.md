## Context

All data required for macro aggregation already exists across four tables: `meal_logs`, `meal_items`, `foods`, and `nutritional_goals`. The dashboard slice is purely read-only: it joins and aggregates that data into per-day and per-week summaries for the frontend charts and summary card. No new tables or Liquibase changesets are required.

The existing slices expose `MealLogRepository` and `FoodRepository` as `public` interfaces (made public during the ai-nutrition-coach implementation). `NutritionalGoalRepository` is also public. The dashboard service can read from all three without further visibility changes.

## Goals / Non-Goals

**Goals:**
- Compute macro totals (calories, protein, carbs, fat) from raw meal logs for a given day or 7-day window
- Compare computed totals against the user's nutritional goal targets
- Expose three read-only endpoints under `/api/v1/dashboard`
- Test via unit tests (Mockito) and an integration test (Testcontainers)

**Non-Goals:**
- Per-meal-type breakdown (already in meal-logging slice)
- Streak tracking, trend scoring, or goal achievement history
- Caching or performance optimisation (acceptable to recompute on every request at this stage)
- Schema changes

## Decisions

### 1. Compute totals in the service layer, not via SQL aggregation
Macro totals are calculated in Java by iterating `MealLog → MealItem → Food` and accumulating `(quantityG / 100) × nutrientPer100g` per macro. This avoids a custom JPQL/native query and keeps the logic testable with plain Mockito mocks.

*Alternative considered:* A `@Query` with `SUM` and `JOIN` — more efficient at scale but harder to test and premature for an alpha.

### 2. Reuse existing cross-slice repositories directly
`DashboardService` injects `MealLogRepository`, `FoodRepository`, and `NutritionalGoalRepository` directly (all public). No new query methods are needed — `findByUserIdAndLoggedAtBetween` on `MealLogRepository` covers both daily and weekly windows.

*Alternative considered:* A dedicated `DashboardRepository` with a native SQL aggregate query — more performant but adds complexity for no current benefit.

### 3. Null-safe macro accumulation
Foods may have null nutrient fields (sourced from USDA where not all fields are populated). The service treats null as zero, matching the pattern used in `AdvicePromptBuilder`.

### 4. Goals are optional in the response
If the user has no nutritional goal set, `targets` fields in the response are `null` rather than returning HTTP 404 — the dashboard is still useful without a goal.

### 5. `date` query parameter, not path segment
`GET /api/v1/dashboard/daily?date=2026-05-21` and `GET /api/v1/dashboard/weekly?weekStart=2026-05-19` use query params (consistent with the `/api/v1/advice` filter pattern).

## Risks / Trade-offs

- **N+1 on food lookups** → `findById` is called per `MealItem`; acceptable for typical meal sizes (5–20 items/day). Mitigated later with a `findAllById` batch if profiling shows it matters.
- **No goal set** → targets returned as `null`; frontend must handle gracefully.
- **Timezone** → `logged_at` is stored as `TIMESTAMPTZ` (UTC). The daily window is computed as midnight-to-midnight UTC. Frontend must pass dates in UTC or the summary may appear off by one day for users in non-UTC timezones. Documented as a known limitation.

## Migration Plan

No schema migration. Deploy as a new vertical slice under `com.epam.macromind.dashboard`. No rollback concern — read-only endpoints with no side effects.

## Open Questions

None.
