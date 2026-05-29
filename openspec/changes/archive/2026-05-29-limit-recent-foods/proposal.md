## Why

The recent foods list currently has no enforced upper bound visible to the user — the frontend requests up to 10 items but the backend `limit` parameter is accepted without a documented maximum, and the spec says "default 5" without a hard cap. Users who log many foods end up with a long, unwieldy quick-pick list that defeats its purpose as a fast-access shortcut. Capping the list at 10 items provides a predictable, scannable list without requiring scrolling.

## What Changes

- The `GET /api/v1/foods/recent` endpoint will enforce a hard maximum of 10 results, ignoring any `?limit=` value above 10.
- The existing spec requirement ("up to a configurable limit, default 5") will be updated to "up to a configurable limit, default 5, maximum 10".
- The frontend `FoodItemForm` currently passes `?limit=10` — no frontend change needed since 10 is the new cap.

## Non-goals

- Changing the default from 5 to a different number.
- Adding pagination to the recent foods endpoint.
- Persisting a per-user preference for the list size.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `recent-foods`: Hard cap of 10 on the recent foods endpoint (requirement and scenario updated)

## Impact

- **Backend**: `FoodService.getRecentFoods` — the server-side cap is already `Math.min(limit, 10)`; the spec needs to reflect this, and the cap value should be a named constant.
- **API**: `GET /api/v1/foods/recent` — behaviour unchanged for callers passing `limit ≤ 10`; callers passing `limit > 10` silently receive 10 items (already the case in code).
- **Frontend**: No change — `MealLog.tsx` already passes `?limit=10`.
- **Tests**: Existing integration test for the cap should be verified/added if missing.
- **Database tables**: `meal_items`, `foods` (read-only query — no schema changes).
