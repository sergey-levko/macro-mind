## Context

Users have profiles and a food catalog but no mechanism to record daily meals. The `meal_logs` and `meal_items` tables already exist in the schema. This change implements the meal vertical slice: two JPA entities, their repositories, a service layer, and a REST controller. Macro totals are derived from food data at read time.

## Goals / Non-Goals

**Goals:**
- CRUD for meal logs (a dated meal slot with a meal type)
- Add / remove food items within a meal log, each with a quantity in grams
- Return computed macro totals (calories, protein, carbs, fat) per item and per log in responses
- List a user's meal logs filtered by date

**Non-Goals:**
- Editing a meal item's quantity (delete + re-add)
- Macro aggregation across multiple logs / days (future dashboard change)
- Nutritional goal comparison
- AI advice

## Decisions

### 1. Compute macros on-the-fly, not stored

Macro values are computed at read time: `nutrient = food.nutrient_per_100g × (quantity_g / 100)`. They are NOT persisted in `meal_items`.

**Rationale:** Storing derived data creates a sync problem if food macro data is ever corrected. The computation is trivial and the result set is small (at most a few dozen items per log). No migration needed when food data changes.

**Alternative considered:** Store snapshot values at insert time (immutable history). Rejected — overkill at this stage; no edit history requirement exists.

### 2. Single `meal` vertical slice for both `MealLog` and `MealItem`

Both entities live in `com.epam.macromind.meal`. `MealItem` is an aggregate member of `MealLog` — they are always accessed together.

**Rationale:** Keeps the slice cohesive; `MealItem` has no independent lifecycle outside a `MealLog`.

### 3. Authorization via `X-User-Id` header (consistent with existing pattern)

All endpoints receive `X-User-Id` as a request header. Service validates ownership before any mutation. No Spring Security at this stage.

### 4. `logged_at` stored as `TIMESTAMP WITH TIME ZONE`, date filtering in JPQL

`GET /api/v1/meal-logs?date=2024-01-15` filters using a JPQL range query: `logged_at >= startOfDay AND logged_at < startOfNextDay` in UTC. Client is responsible for providing ISO-8601 date.

**Rationale:** Simple, no extra DB column; works correctly with UTC timestamps.

### 5. Deleting a meal log cascades to its items

`MealLog` → `MealItem` uses `CascadeType.ALL` + `orphanRemoval = true`. Deleting a log removes all its items atomically.

## Risks / Trade-offs

- **No quantity edit** → Users must delete and re-add items to correct a quantity. Acceptable for MVP; UX can hide this detail.
- **UTC-only date filtering** → If a user is in a non-UTC timezone, "today" may not match expectations. Accepted for now; timezone support is a future concern.
- **Food ownership not enforced on item add** → A user can add any food (including another user's custom food) to their meal. The food catalog doesn't restrict visibility yet. Acceptable since foods are nutritional data, not sensitive.

## Migration Plan

1. Add Liquibase changesets for `meal_logs` and `meal_items` (changeset IDs `0004-meal-logs` and `0005-meal-items`)
2. Deploy — no data migration needed (new tables, no existing data affected)
3. Rollback: drop `meal_items` first (FK dependency), then `meal_logs`
