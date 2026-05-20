## Why

Users have profiles and a food catalog but no way to record what they actually eat. Meal logging is the core data collection layer that all other features — macro dashboards, nutritional goals tracking, and AI advice — depend on. Without it, MacroMind has no data to analyze.

## What Changes

- New API to create, retrieve, and delete meal logs (breakfast/lunch/dinner/snack)
- New API to add and remove food items within a meal log, each with a quantity in grams
- Macro totals (calories, protein, carbs, fat) computed on-the-fly from `foods.calories_100g` etc. × `quantity_g / 100`
- Endpoints to list all meal logs for a user on a given date

## Capabilities

### New Capabilities

- `meal-logging`: Create and manage meal logs with typed meal slots (BREAKFAST/LUNCH/DINNER/SNACK); add/remove food items with quantity; retrieve logs by date with computed macro totals per item and per log.

### Modified Capabilities

_(none — no existing spec requirements change)_

## Non-goals

- Meal templates or saved meal presets
- Editing a meal item's quantity after creation (delete and re-add instead)
- Calorie/macro goal comparison or progress charts (covered by a future nutritional-goals change)
- AI advice generation (separate change)

## Impact

- **New tables**: `meal_logs`, `meal_items` (Liquibase changesets required)
- **New vertical slice**: `com.epam.macromind.meal` — entity, repository, service, controller, DTOs
- **Reads from**: `foods` table (macro data per 100g), `users` table (ownership validation)
- **New endpoints**: `POST /api/v1/meal-logs`, `GET /api/v1/meal-logs?date=`, `GET /api/v1/meal-logs/{id}`, `DELETE /api/v1/meal-logs/{id}`, `POST /api/v1/meal-logs/{id}/items`, `DELETE /api/v1/meal-logs/{id}/items/{itemId}`
