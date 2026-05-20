## 1. Domain Model

- [x] 1.1 Create `NutritionalGoal` JPA entity (`com.epam.macromind.goal`) mapped to `nutritional_goals` table with fields: id (UUID), userId (UUID), caloriesTarget, proteinG, carbsG, fatG (BigDecimal)
- [x] 1.2 Create `NutritionalGoalRepository` (JpaRepository) with `findByUserId(UUID userId): Optional<NutritionalGoal>`

## 2. Service Layer

- [x] 2.1 Create `GoalNotFoundException` (extends `RuntimeException`) and `SetNutritionalGoalRequest` / `NutritionalGoalResponse` DTOs
- [x] 2.2 Create `NutritionalGoalService` with `setGoal`, `getGoal`, and `deleteGoal` methods; upsert logic: find existing by userId → delete if present → save new entity

## 3. REST Controller

- [x] 3.1 Create `NutritionalGoalController` (`PUT /api/v1/nutritional-goals` → 200, `GET /api/v1/nutritional-goals` → 200, `DELETE /api/v1/nutritional-goals` → 204) reading `X-User-Id` header
- [x] 3.2 Register `GoalNotFoundException` → HTTP 404 in `GlobalExceptionHandler`

## 4. Tests

- [x] 4.1 Write `NutritionalGoalServiceTest` (Mockito): set new goal, replace existing goal, get goal, get goal not found, delete goal, delete goal not found, set goal with unknown user
- [x] 4.2 Write `NutritionalGoalControllerTest` (`@WebMvcTest`): PUT 200, PUT 400 missing fields, GET 200, GET 404, DELETE 204, DELETE 404
- [x] 4.3 Write `NutritionalGoalIntegrationTest` (Testcontainers): full round-trip (set → get → replace → delete), 404 on missing goal
