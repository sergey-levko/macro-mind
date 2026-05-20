## 1. Git Branch

- [x] 1.1 Create and switch to branch `feat/meal-logging`

## 2. Database Schema

- [x] 2.1 Create Liquibase changeset `0004-meal-logs.yaml` — `meal_logs` table (id UUID PK, user_id UUID FK → users, meal_type VARCHAR, logged_at TIMESTAMPTZ); commit `feat: add meal_logs Liquibase changeset`
- [x] 2.2 Create Liquibase changeset `0005-meal-items.yaml` — `meal_items` table (id UUID PK, meal_log_id UUID FK → meal_logs ON DELETE CASCADE, food_id UUID FK → foods, quantity_g DECIMAL); commit `feat: add meal_items Liquibase changeset`

## 3. Meal Domain Slice

- [x] 3.1 Create `MealLog` JPA entity mapped to `meal_logs`, `MealItem` JPA entity mapped to `meal_items` (with `CascadeType.ALL` + `orphanRemoval` on the `items` collection); create `MealType` enum (BREAKFAST/LUNCH/DINNER/SNACK); create `MealLogRepository` (extends `JpaRepository<MealLog, UUID>`) with `findByUserIdAndLoggedAtBetween`; create `MealItemRepository` (extends `JpaRepository<MealItem, UUID>`); commit `feat: add MealLog and MealItem entities and repositories`
- [x] 3.2 Create `CreateMealLogRequest` (`@NotNull MealType mealType`, optional `Instant loggedAt`), `AddMealItemRequest` (`@NotNull UUID foodId`, `@NotNull @Positive BigDecimal quantityG`), `MealItemResponse` (itemId, foodId, foodName, quantityG, calories, proteinG, carbsG, fatG), `MacroTotals` (calories, proteinG, carbsG, fatG), `MealLogResponse` (id, userId, mealType, loggedAt, items, totals), `MealLogSummaryResponse` (id, mealType, loggedAt, totals); commit `feat: add meal log request/response DTOs`
- [x] 3.3 Implement `MealService` with: `createMealLog(UUID userId, CreateMealLogRequest)` (validates user exists, defaults `loggedAt` to now), `getMealLogById(UUID)` (throws `MealLogNotFoundException`), `getMealLogsByDate(UUID userId, LocalDate date)` (JPQL range query on `logged_at`), `deleteMealLog(UUID userId, UUID logId)` (throws `MealLogNotFoundException` or `MealLogAccessDeniedException`), `addItem(UUID userId, UUID logId, AddMealItemRequest)` (validates log ownership and food existence, computes macros), `removeItem(UUID userId, UUID logId, UUID itemId)` (validates ownership, throws `MealItemNotFoundException`); commit `feat: add MealService`
- [x] 3.4 Implement `MealController` with endpoints: `POST /api/v1/meal-logs` (201), `GET /api/v1/meal-logs/{id}` (200), `GET /api/v1/meal-logs?date=` (200), `DELETE /api/v1/meal-logs/{id}` (204), `POST /api/v1/meal-logs/{id}/items` (201), `DELETE /api/v1/meal-logs/{id}/items/{itemId}` (204); add `GlobalExceptionHandler` entries for `MealLogNotFoundException` (404), `MealLogAccessDeniedException` (403), `MealItemNotFoundException` (404); commit `feat: add MealController and exception handling`

## 4. Unit Tests

- [x] 4.1 Write `MealServiceTest` using Mockito: create log success, user not found → 404, get log success, get log not found → 404, get by date returns results, delete success, delete not found → 404, delete forbidden → 403, add item success, add item log not found → 404, add item food not found → 404, add item forbidden → 403, remove item success, remove item not found → 404, remove item forbidden → 403; commit `test: add MealService unit tests`
- [x] 4.2 Write `MealControllerTest` using `@WebMvcTest`: 201 on create log, 400 on missing mealType, 404 on unknown user, 200 on get log, 404 on unknown log, 200 on list by date, 400 on missing date, 204 on delete, 403 on forbidden delete, 201 on add item, 400 on invalid quantityG, 404 on unknown food, 204 on remove item; commit `test: add MealController unit tests`

## 5. Integration Tests

- [x] 5.1 Write `MealIntegrationTest` using Testcontainers + `@SpringBootTest`: create log and retrieve round-trip, add item computes correct macros, list by date returns only that day's logs, delete log removes log and items, add item to another user's log returns 403; commit `test: add meal logging integration tests`

## 6. Pull Request

- [ ] 6.1 Push branch `feat/meal-logging` to remote and open a pull request targeting `master`
