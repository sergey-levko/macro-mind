## Why

Users can add foods to the database (manually or via USDA import) but have no way to correct mistakes or remove outdated entries. A dedicated Foods tab gives users full CRUD control over the food library they've built.

## What Changes

- Add a **Foods** navigation tab to the frontend sidebar
- Add a food list page that displays all foods with name, calories, and macros
- Add inline editing of food fields (name, calories/100g, protein, carbs, fat)
- Add delete action per food item
- Expose `PUT /api/v1/foods/{id}` and `DELETE /api/v1/foods/{id}` backend endpoints

## Capabilities

### New Capabilities
- `food-management`: Browse, edit, and delete foods stored in the database via a dedicated Foods tab

### Modified Capabilities

## Impact

- **Backend**: New `PUT` and `DELETE` endpoints on `FoodController`; `FoodService` gains update and delete methods
- **Frontend**: New `Foods` page component; router and sidebar nav updated with a new tab
- **Data**: Deleting a food that is referenced by existing `meal_items` must be handled (reject or cascade — to be decided in design)
