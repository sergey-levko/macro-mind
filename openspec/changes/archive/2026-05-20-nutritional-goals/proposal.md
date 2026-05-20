## Why

Users can log meals and see macro totals, but there is no way to set personal targets — making it impossible to know whether a day's intake is on track. Nutritional goals unlock the "target vs actual" comparison that is the core value loop of the app, and are a prerequisite for AI-generated advice.

## What Changes

- New CRUD API for a user's nutritional goal (one per user): create/replace, retrieve, delete
- The `nutritional_goals` table is already in the schema; this change adds the application layer on top of it
- A user may have at most one active goal at a time (upsert semantics on create)

## Capabilities

### New Capabilities
- `nutritional-goals`: Manage a user's daily macro targets (calories, protein, carbs, fat). Supports create/replace, retrieve, and delete via REST API.

### Modified Capabilities

## Impact

- **Database**: `nutritional_goals` table (id, user_id FK, calories_target, protein_g, carbs_g, fat_g) — already migrated; no new changesets needed
- **New API endpoints**: `PUT /api/v1/nutritional-goals`, `GET /api/v1/nutritional-goals`, `DELETE /api/v1/nutritional-goals` (all scoped to `X-User-Id`)
- **New vertical slice**: `com.epam.macromind.goal` — entity, repository, service, controller, DTOs, exception classes
- **GlobalExceptionHandler**: add handler for `GoalNotFoundException`

## Non-goals

- Multiple concurrent goals or goal history/versioning
- Per-meal or per-food-item targets
- Macro recommendations derived from user profile (that belongs to the AI advice feature)
- Frontend implementation
