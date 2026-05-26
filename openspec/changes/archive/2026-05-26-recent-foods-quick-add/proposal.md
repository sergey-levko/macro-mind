## Why

Users repeat the same foods daily but must re-search every time they log a meal, adding friction to a routine action. Surfacing the 5–10 most recently used foods directly in the meal log UI eliminates most of those searches for typical logging sessions.

## What Changes

- New API endpoint `GET /api/v1/foods/recent` returns the user's most recently used distinct foods (up to 10), ordered by last-used descending
- Meal log food-add panel shows a "Recent" section above the search input when the search field is empty, listing the recent foods as one-tap add buttons
- Recent list is hidden once the user starts typing a search query

## Capabilities

### New Capabilities
- `recent-foods`: Returns the N most recently used distinct foods per user, derived from `meal_items` / `meal_logs` join; drives the quick-add UI in the meal log

### Modified Capabilities
- `frontend-meal-logging`: Add the recent foods quick-add section above the search input in the food-add panel

## Impact

**Database tables:** `meal_logs`, `meal_items`, `foods` (read-only query — no schema changes)

**New API endpoints:**
- `GET /api/v1/foods/recent?limit=10` — returns `List<FoodResponse>` for the authenticated user's most recently used distinct foods

**Modified files:**
- `frontend/src/pages/MealLog.tsx` — render recent foods section when search is empty
- Backend food slice: new controller method + service query

**Non-goals:**
- Frequency-based ranking (most-used vs most-recent) — recency is simpler and already useful
- Persistent "favorites" or pinning — out of scope
- Cross-device sync considerations — all data already stored in PostgreSQL per user
- Modifying the foods table or adding new columns
