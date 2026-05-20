## 1. Git Branch

- [x] 1.1 Create and switch to branch `feat/food-catalog`

## 2. Food Domain Slice

- [x] 2.1 Create `Food` JPA entity mapped to the `foods` table and `FoodRepository` (extends `JpaRepository<Food, UUID>`) with a custom `findByUserIdAndNameContainingIgnoreCase` query method; commit `feat: add Food entity and FoodRepository`
- [x] 2.2 Create `CreateFoodRequest` (Bean Validation: `@NotBlank` on name, `@NotNull` on macro fields), `ImportFoodRequest` (`@NotNull fdcId`), and `FoodResponse` DTOs; commit `feat: add food request/response DTOs`
- [x] 2.3 Create `UsdaFoodClient` using Spring `RestClient` to call `GET https://api.nal.usda.gov/fdc/v1/food/{fdcId}?api_key=...`; map response to an internal `UsdaFoodDto`; throw `UsdaFoodNotFoundException` on 404 and `UsdaServiceUnavailableException` on connection errors; inject `USDA_API_KEY` from environment; commit `feat: add UsdaFoodClient`
- [x] 2.4 Implement `FoodService` with: `createFood(UUID userId, CreateFoodRequest)` (validates user exists via `UserRepository`, sets `source = "CUSTOM"`), `getFoodById(UUID)` (throws `FoodNotFoundException` on miss), `searchFoods(UUID userId, String search)` (returns up to 20 results), `deleteFood(UUID userId, UUID foodId)` (throws `FoodNotFoundException` or `FoodAccessDeniedException`), `importFood(UUID userId, ImportFoodRequest)` (delegates to `UsdaFoodClient`, persists with `source = "USDA"`); commit `feat: add FoodService`
- [x] 2.5 Implement `FoodController` with endpoints: `POST /api/v1/foods` (201), `GET /api/v1/foods/{id}` (200), `GET /api/v1/foods` (200), `DELETE /api/v1/foods/{id}` (204), `POST /api/v1/foods/import` (201); add `GlobalExceptionHandler` entries for `FoodNotFoundException` (404), `FoodAccessDeniedException` (403), `UsdaFoodNotFoundException` (404), `UsdaServiceUnavailableException` (503); commit `feat: add FoodController and exception handling`

## 3. Unit Tests

- [x] 3.1 Write `FoodServiceTest` using Mockito: create success, user not found → 404, get by id success, get by id not found → 404, search returns results, delete success, delete not found → 404, delete forbidden → 403, import success, import USDA not found → 404, import USDA unavailable → 503; commit `test: add FoodService unit tests`
- [x] 3.2 Write `FoodControllerTest` using `@WebMvcTest`: 201 on create, 400 on missing fields, 404 on unknown user, 200 on get, 404 on unknown food, 400 on invalid UUID, 200 on search, 204 on delete, 403 on forbidden delete, 201 on import, 404 on unknown fdcId, 503 on USDA unavailable; commit `test: add FoodController unit tests`

## 4. Integration Tests

- [x] 4.1 Write `FoodIntegrationTest` using Testcontainers + `@SpringBootTest`: create and retrieve food round-trip, search by name returns match, delete removes food, delete another user's food returns 403, import from USDA (mocked HTTP server via WireMock) persists food with `source = "USDA"`; commit `test: add food catalog integration tests`

## 5. Pull Request

- [x] 5.1 Push branch `feat/food-catalog` to remote and open a pull request targeting `master`
