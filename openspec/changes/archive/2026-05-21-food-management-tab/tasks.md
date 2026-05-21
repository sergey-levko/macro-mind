## 1. Backend — Update endpoint

- [x] 1.1 Create `UpdateFoodRequest` record with `name`, `calories100g`, `proteinG`, `carbsG`, `fatG` fields and validation annotations
- [x] 1.2 Add `updateFood(UUID userId, UUID foodId, UpdateFoodRequest request)` to `FoodService` (ownership check + save)
- [x] 1.3 Add `PUT /api/v1/foods/{id}` to `FoodController` returning updated `FoodResponse`

## 2. Backend — Delete guard

- [x] 2.1 Create `FoodInUseException` and map it to `409 Conflict` in the global exception handler
- [x] 2.2 Add `MealItemRepository.existsByFoodId(UUID foodId)` method
- [x] 2.3 Guard `FoodService.deleteFood` — throw `FoodInUseException` if any meal item references the food

## 3. Frontend — Foods page

- [x] 3.1 Create `src/pages/Foods.tsx` with a search input and a list of the user's foods (fetch `GET /api/v1/foods?search=`)
- [x] 3.2 Render each food row showing name, calories/100g, protein, carbs, fat with Edit and Delete buttons
- [x] 3.3 Implement inline edit form per row (pre-filled fields; Save/Cancel; calls `PUT /api/v1/foods/{id}`)
- [x] 3.4 Implement delete action with confirmation; display the 409 error message returned by the backend
- [x] 3.5 Add `api.put` to `src/lib/api.ts` if not already present

## 4. Frontend — Navigation

- [x] 4.1 Add route `path="foods" element={<Foods />}` inside the authenticated layout in `App.tsx`
- [x] 4.2 Add `<NavLink to="/foods">Foods</NavLink>` entry to the sidebar in `Layout.tsx`
