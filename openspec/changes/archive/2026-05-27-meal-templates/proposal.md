## Why

Users who eat the same meals regularly — a fixed breakfast, a standard work lunch — have to re-log or copy from the previous day every time. A named template system lets them capture a set of meals once and replay it on any date, reducing logging friction for habitual eaters more effectively than the one-shot copy-yesterday feature.

## What Changes

- Add a `meal_templates` table that stores named collections of template items (food + quantity, no date)
- Add a `meal_template_items` table with food ID, quantity, and meal type per item
- Add REST endpoints: `POST /api/v1/meal-templates` (save), `GET /api/v1/meal-templates` (list), `DELETE /api/v1/meal-templates/{id}` (delete), `POST /api/v1/meal-templates/{id}/apply` (apply to a date)
- Add a Templates tab to the Meal Log page: create a template from today's logged meals, view saved templates, apply a template to the current date, delete templates

## Capabilities

### New Capabilities

- `meal-templates`: Save named collections of meal items and apply them to any date to pre-populate meal logs

### Modified Capabilities

- `frontend-meal-logging`: Meal Log page gains a Templates tab for managing and applying templates

## Impact

- **New tables**: `meal_templates` (id, user_id, name, created_at), `meal_template_items` (id, template_id, food_id, meal_type, quantity_g)
- **New API endpoints**: `POST /api/v1/meal-templates`, `GET /api/v1/meal-templates`, `DELETE /api/v1/meal-templates/{id}`, `POST /api/v1/meal-templates/{id}/apply`
- **Affected tables (read)**: `meal_logs`, `meal_items`, `foods`
- **Frontend**: `MealLog.tsx` gains a new Templates tab section

## Non-goals

- No template editing (delete and recreate instead)
- No partial template apply (applying a template always creates all items)
- No template sharing between users
- No scheduling or auto-apply on specific days
- No integration with AI advice or nutritional goals
