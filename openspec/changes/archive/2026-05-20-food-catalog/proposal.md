## Why

MacroMind users need a food catalog before they can log meals or track macros. Without foods in the system, the core meal-logging and nutritional-analysis features cannot function. The `foods` table exists in the schema but has no API or service layer yet.

## What Changes

- Add `POST /api/v1/foods` — create a custom food entry with full macro data
- Add `GET /api/v1/foods/{id}` — retrieve a single food by UUID
- Add `GET /api/v1/foods?search=` — search foods by name (user-scoped + shared/USDA foods)
- Add `DELETE /api/v1/foods/{id}` — delete a user-owned custom food
- Add `POST /api/v1/foods/import` — import a food from USDA FoodData Central by `fdcId`, persisting it locally in the `foods` table

## Capabilities

### New Capabilities

- `food-catalog`: CRUD for user-created foods and USDA import; covers the `foods` table and search endpoint

### Modified Capabilities

<!-- None -->

## Non-goals

- Editing existing food entries (update endpoint) — out of scope for this iteration
- Bulk import from USDA — only single-item import by `fdcId`
- Frontend UI — backend API only
- Nutritional goal tracking or AI advice — handled in future changes

## Impact

- **Database**: `foods` table (`id`, `user_id FK`, `name`, `source`, `calories_100g`, `protein_g`, `carbs_g`, `fat_g`) — no schema changes needed, table already defined in the DB schema
- **New endpoints**: `POST`, `GET`, `DELETE /api/v1/foods`, `POST /api/v1/foods/import`
- **External dependency**: USDA FoodData Central REST API (`api.nal.usda.gov`) — requires an API key configured via environment variable
- **Affected slices**: new `food` vertical slice (entity, repository, service, controller, DTOs)
