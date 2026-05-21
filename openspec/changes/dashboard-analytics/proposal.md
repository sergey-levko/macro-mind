## Why

The frontend dashboard (bar/line charts for daily and weekly macros) has no backend data source. All other backend slices are complete, but without analytics endpoints the charts screen is empty and the app cannot be demoed end-to-end.

## What Changes

- **New** `GET /api/v1/dashboard/daily` — returns macro totals (calories, protein, carbs, fat) for a single day, compared against the user's nutritional goal targets
- **New** `GET /api/v1/dashboard/weekly` — returns per-day macro totals for a 7-day window, plus weekly aggregated totals vs targets
- **New** `GET /api/v1/dashboard/summary` — returns a lightweight snapshot: today's intake, goal targets, and percentage completion per macro; designed for a top-of-screen summary card

## Capabilities

### New Capabilities
- `dashboard-analytics`: Daily and weekly macro aggregation endpoints that compute calorie/protein/carbs/fat totals from `meal_logs` + `meal_items` + `foods`, compared against `nutritional_goals` targets

### Modified Capabilities
<!-- none -->

## Impact

- **Database tables read:** `meal_logs`, `meal_items`, `foods`, `nutritional_goals`
- **New endpoints:** `GET /api/v1/dashboard/daily`, `GET /api/v1/dashboard/weekly`, `GET /api/v1/dashboard/summary`
- **No schema changes** — read-only aggregation over existing tables
- **Non-goals:** writing/mutating data, per-meal breakdown (already in meal-logging slice), streaks or trend scoring, frontend implementation
