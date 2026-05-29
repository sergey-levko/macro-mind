## 1. Backend — Extract constant and enforce cap

- [x] 1.1 In `FoodService`, replace the inline `10` in `getRecentFoods` with a named constant `private static final int MAX_RECENT_FOODS = 10`. Commit: `refactor: extract MAX_RECENT_FOODS constant in FoodService`

## 2. Backend — Tests

- [x] 2.1 In `FoodIntegrationTest`, add a test `getRecentFoods_limitAboveCap_returnsCappedResults` that logs more than 10 distinct foods, calls `GET /api/v1/foods/recent?limit=50`, and asserts the response contains exactly 10 items. Commit: `test: add integration test for recent foods limit cap`
