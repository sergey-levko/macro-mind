## Context

Foods can be created (manually or via USDA import) and searched, but users cannot correct mistakes or remove entries. `DELETE /api/v1/foods/{id}` already exists in `FoodController` but `PUT` does not. The `food_id` FK in `meal_items` has no `ON DELETE CASCADE`, so deleting a food referenced by meal items would cause a DB constraint violation at the service layer without a guard.

Frontend has no Foods page; the nav/router pattern uses `<NavLink>` in `Layout.tsx` and nested routes in `App.tsx`.

## Goals / Non-Goals

**Goals:**
- Add a Foods tab with a paginated/searchable list of the user's foods
- Allow inline editing of food fields (name, calories/100g, protein, carbs, fat)
- Allow deletion of foods with a clear error when the food is in use
- Add `PUT /api/v1/foods/{id}` backend endpoint
- Guard `deleteFood` against FK violations and surface a user-friendly error

**Non-Goals:**
- Bulk delete or bulk edit
- Transferring food ownership between users
- Cascading deletes of meal items when a food is removed

## Decisions

**Delete behavior — reject if in use (not cascade)**
Silently cascading would corrupt historical meal logs. Instead, return `409 Conflict` with a message like "Food is used in meal logs and cannot be deleted." The frontend shows this message to the user.
*Alternative considered:* `ON DELETE CASCADE` at DB level — rejected because it destroys nutritional history.

**Edit via PUT (full replace, not PATCH)**
All five editable fields are always shown together in the form, so sending the full resource is simpler than a partial PATCH. There are no fields that should be immutable after creation.

**Inline editing UX**
Each row has an Edit button that expands an inline form (same pattern as `FoodItemForm` in MealLog). Avoids a separate edit page and keeps the interaction lightweight.
*Alternative considered:* modal dialog — rejected to keep the UI consistent with existing inline patterns.

**Search in Foods tab**
Reuse the existing `GET /api/v1/foods?search=` endpoint to filter the list, consistent with how the meal log's food search works.

## Risks / Trade-offs

- [FK violation on delete] → Caught in `FoodService.deleteFood` via `DataIntegrityViolationException`; re-thrown as a typed `FoodInUseException` mapped to `409 Conflict`.
- [Stale displayed data after edit/delete] → Optimistic list refresh after each mutation (re-fetch the list).

## Migration Plan

No schema changes required. Backend adds one endpoint and one exception type. Frontend adds one page and one nav entry. No data migration needed.

## Open Questions

- Should the Foods tab show only the current user's foods, or all foods? (Current `searchFoods` already filters by `userId` — keeping that scope.)
