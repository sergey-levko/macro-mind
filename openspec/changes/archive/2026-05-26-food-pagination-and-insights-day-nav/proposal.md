## Why

The Foods tab currently caps results at 20 items with no way to see older entries, making larger food libraries inaccessible. The Insights tab's Daily view is locked to today — users can't browse past daily insights without switching to the flat History view, which lacks the clean day-by-day mental model already established in Meal Logs.

## What Changes

- `GET /api/v1/foods` gains a `page` query parameter (0-based); response wraps results in a page envelope with `content`, `page`, `totalPages`, and `totalElements`
- The Foods tab frontend gains "Previous" / "Next" page buttons and a page indicator; the existing search resets to page 0 on each new query
- The Insights tab replaces the standalone "Daily" sub-tab with a day-navigation view (prev/next buttons + date picker matching the Meal Log pattern); navigating shows the saved DAILY insight for that date, if any, plus the "Generate" button
- The "Weekly" sub-tab is unchanged
- The "History" sub-tab is removed — the new day-navigation view makes it redundant

## Non-goals

- Pagination for the USDA food search results
- Pagination for meal logs or advice history via API
- Changing Weekly insight navigation (weekly date pickers are more complex and lower priority)
- Sorting or ordering controls for the food list

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `food-management`: Adding paginated food list requirement — `GET /api/v1/foods` now returns a `Page<FoodResponse>` instead of `List<FoodResponse>`, with optional `page` (default 0) and `size` (default 20) query parameters
- `coach-insights`: Adding day-navigation requirement to the Daily insights view — users can browse insights by day using prev/next buttons and a date picker, starting at today

## Impact

- **Backend**: `FoodController`, `FoodService`, `FoodRepository` — replace `findTop20` with `findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc` + `Pageable`, update response wrapper; no Liquibase changes
- **Frontend**: `Foods.tsx` — add pagination state and controls; `Coach.tsx` — replace Daily tab static date with navigable date state, remove History sub-tab and its state/effects
- **API**: `GET /api/v1/foods` response shape changes (breaking for any existing callers relying on array root)
- **Affected tables**: `foods`
- **Affected endpoints**: `GET /api/v1/foods`
