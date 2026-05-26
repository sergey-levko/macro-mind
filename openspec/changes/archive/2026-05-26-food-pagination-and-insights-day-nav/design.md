## Context

The `foods` table has no hard size bound per user, but `FoodRepository` currently caps responses at 20 via Spring Data method names (`findTop20ByUserId`, `findTop20ByUserIdAndNameContainingIgnoreCase`). This means users with more than 20 foods silently lose access to the rest.

The Insights tab has three sub-tabs: Daily (static today), Weekly (static current week), and History (flat paginated-less list). The Meal Log uses a proven day-navigation pattern — prev/next buttons + date picker — that maps naturally to daily insight browsing. Unifying the two removes the History sub-tab and its separate state management.

No schema changes are required for either feature.

## Goals / Non-Goals

**Goals:**
- Expose proper page-based pagination on `GET /api/v1/foods` so all user foods are reachable
- Replace the static Daily + History sub-tabs with a single day-navigation view (matching Meal Log UX)
- Keep USDA search and import unaffected

**Non-Goals:**
- Weekly insight day navigation
- Sorting or filtering controls for the food list
- Cursor-based / infinite-scroll pagination
- Any database schema changes

## Decisions

### Food pagination: Spring Data `Page<T>` with `Pageable`

Replace the two `findTop20` repository methods with:
```
Page<Food> findByUserId(UUID userId, Pageable pageable);
Page<Food> findByUserIdAndNameContainingIgnoreCase(UUID userId, String name, Pageable pageable);
```
Controller accepts `?page=0&size=20` (size capped at 50 server-side). Response changes from `List<FoodResponse>` to a `PageResponse<FoodResponse>` wrapper with `content`, `page`, `totalPages`, `totalElements`.

**Why not keep the array format with a `Link: rel=next` header?** The frontend needs `totalPages` to know whether to show the Next button and display "Page X of Y". A JSON envelope is simpler to consume from React than response headers.

**Why not infinite scroll / "Load more"?** The food list supports inline edit + delete. With infinite scroll the list grows unbounded and delete/edit operations still require knowing the current page for re-fetching. Page buttons keep the state model simple.

**Response envelope:**
```json
{
  "content": [...],
  "page": 0,
  "totalPages": 3,
  "totalElements": 47
}
```

### Insights day navigation: frontend-only date state

The backend already accepts `?adviceType=DAILY&periodStart=<date>`, so day navigation is entirely a frontend change. Lift the hardcoded `todayStr()` call in the existing load effect into a `selectedDate` state variable (defaulting to today). Prev/next buttons call `shiftDay(selectedDate, ±1)`, capped at today (future dates not allowed). Reuse the existing `HistoryDatePicker` component (or its internals) to provide the calendar picker.

**Why remove the History sub-tab?** The day-navigation view subsumes it: navigating backwards browses all past daily insights. Keeping History alongside would create two overlapping ways to find the same data.

**Why keep Weekly unchanged?** Weekly insights are less granular (one per week) and users don't typically need to browse past weeks the same way. Adding week navigation is lower priority and can be a follow-up.

### Insight tab: state simplification

The current Coach component tracks `insightPeriod: 'daily' | 'weekly' | 'history'`. After this change it becomes `insightPeriod: 'daily' | 'weekly'` and the Daily panel gains `selectedDate` state. Removes ~8 state variables related to history loading/filtering.

## Risks / Trade-offs

- **Breaking API change** → `GET /api/v1/foods` response root changes from array to object. The only consumer is the Foods tab frontend, which is updated atomically in the same PR.
- **Page staleness after add/delete** → When a food is deleted from page 2, the page count may shift. Mitigation: re-fetch the current page after any mutation (already done via `loadFoods`).
- **Insights for dates with no record** → Day navigation will frequently land on dates with no saved insight. The existing empty-state in `InsightPanel` already handles this correctly.
- **Search resets page** → Changing the search query resets to page 0. This is the expected UX, but it means in-flight page navigation is cancelled on each keystroke. Mitigated by keeping the 300ms debounce.

## Migration Plan

1. Update `FoodRepository` → add `Page<Food>` query methods, remove `findTop20` methods
2. Update `FoodService.searchFoods` → accept `Pageable`, return `PageResponse<FoodResponse>`
3. Update `FoodController.search` → accept `page` / `size` params, return new response type
4. Update `Foods.tsx` → add pagination state and controls
5. Update `Coach.tsx` → replace History sub-tab with day-navigation on Daily panel
6. Update integration tests to verify paginated response shape

No Liquibase changesets required. No rollback steps beyond reverting the PR.

## Open Questions

- Should the food list default sort be alphabetical (by name) or by creation date (most recently added first)? Alphabetical is proposed here for discoverability. If the team prefers recency, swap the `OrderByNameAsc` clause.
