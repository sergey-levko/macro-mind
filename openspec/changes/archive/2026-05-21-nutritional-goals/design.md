## Context

The `nutritional_goals` table is already in the database schema (id, user_id, calories_target, protein_g, carbs_g, fat_g) but has no application layer. The meal-logging slice can compute daily totals; without goals there is nothing to compare them against. This change adds the full vertical slice for the `goal` feature domain.

No schema migration is required — the table already exists. The implementation is a straightforward CRUD slice with upsert semantics (a user has at most one active goal at a time).

## Goals / Non-Goals

**Goals:**
- REST API to set (create or replace), retrieve, and clear a user's nutritional goal
- Single goal per user enforced at the service layer (upsert on PUT)
- Full unit + integration test coverage consistent with existing slices

**Non-Goals:**
- Goal history or versioning
- Macro target recommendations derived from user profile
- Frontend integration
- Per-meal breakdown targets

## Decisions

### 1. PUT for upsert semantics, not POST
The resource is a singleton per user — `GET /api/v1/nutritional-goals` returns the one goal for `X-User-Id`. Using PUT for create-or-replace is idiomatic REST for singleton sub-resources, avoids a separate "update" endpoint, and keeps the client simple.

*Alternative considered:* POST to create + PATCH to update — adds unnecessary complexity for a resource that never has more than one instance.

### 2. Upsert via `deleteAndInsert` at service layer
`NutritionalGoalRepository.findByUserId` retrieves the existing row; if present it is deleted before the new entity is saved. This keeps the entity simple (no nullable fields) and avoids JPA `merge` edge cases.

*Alternative considered:* Hibernate `@NaturalId` on `userId` with `session.merge` — more complex and ties the design to Hibernate internals.

### 3. Vertical slice in `com.epam.macromind.goal`
Follows the established pattern for all slices in this project: `NutritionalGoal` entity → `NutritionalGoalRepository` → `NutritionalGoalService` → `NutritionalGoalController` → DTOs.

### 4. No Liquibase changeset needed
The table was created by the project scaffold changeset. No structural change is required.

## Risks / Trade-offs

- **Concurrent PUT from same user** → last write wins; acceptable for a personal-use single-goal resource with no concurrent editing
- **No validation of goal values against user's TDEE** → values are accepted as-is; AI advice layer can flag unrealistic targets later
- **`X-User-Id` trust model** → consistent with all other slices; no auth layer yet

## Migration Plan

No schema migration required. Deploy as a new vertical slice; existing data in `nutritional_goals` is unaffected.

## Open Questions

None.
