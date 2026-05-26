## 1. Backend — Repository Query

- [x] 1.1 Add `findRecentByUserId` native SQL query to `FoodRepository`: `SELECT f.* FROM foods f JOIN (SELECT mi.food_id, MAX(ml.logged_at) AS last_used FROM meal_items mi JOIN meal_logs ml ON mi.meal_log_id = ml.id WHERE ml.user_id = :userId GROUP BY mi.food_id ORDER BY last_used DESC LIMIT :limit) recent ON f.id = recent.food_id ORDER BY recent.last_used DESC` — returns `List<Food>`

## 2. Backend — Service and Controller

- [x] 2.1 Add `getRecentFoods(UUID userId, int limit)` to `FoodService`: clamp `limit` to `Math.min(limit, 10)`, call repository, map to `FoodResponse`
- [x] 2.2 Add `GET /api/v1/foods/recent` to `FoodController` with `@RequestParam(defaultValue = "10") int limit`, delegating to `foodService.getRecentFoods(userId, limit)`

## 3. Backend — Tests

- [x] 3.1 Add integration test `getRecentFoods_returnsDistinctFoodsOrderedByLastUsed`: register two users; user A logs food X then food Y then food X again; assert response is `[X, Y]` (X most recent) and contains only user A's foods
- [x] 3.2 Add unit test `getRecentFoods_capsLimitAt10`: call `getRecentFoods(userId, 50)`, assert repository invoked with `limit=10`

## 4. Frontend — Recent Foods Quick-Add

- [x] 4.1 In `MealLog.tsx` `FoodItemForm`, add `recentFoods` state (`Food[]`); fetch `GET /api/v1/foods/recent?limit=10` on component mount and store result
- [x] 4.2 Render a "Recent" section above the search input when `searchQuery` is empty and `recentFoods.length > 0`; each food renders as a button that sets it as the selected food; hide section when `searchQuery.length > 0`
