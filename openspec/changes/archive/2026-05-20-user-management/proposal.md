## Why

Every MacroMind feature (food catalog, meal logging, nutrition goals, AI advice) is anchored to a user record. Before any domain feature can be built, the `users` table and its CRUD surface must exist as a stable foundation.

## What Changes

- Liquibase changeset introducing the `users` table with all columns and constraints
- `POST /api/v1/users` — register a new user (name, email, age, weight_kg, height_cm, goal_type)
- `GET /api/v1/users/{id}` — retrieve a user profile by UUID
- User entity, JPA repository, service, and request/response DTOs
- Unit tests (JUnit 5 + Mockito) and integration tests (Testcontainers)

## Capabilities

### New Capabilities

- `user-profile`: Create and retrieve user profiles; exposes the `users` table via REST and establishes the root entity all other features reference

### Modified Capabilities

_(none)_

## Impact

- **Database:** `users` table added via Liquibase changeset (`0002-users.yaml`)
- **API endpoints added:** `POST /api/v1/users`, `GET /api/v1/users/{id}`
- **New backend slice:** `backend/src/main/java/.../user/` (entity, repository, service, controller, DTOs)
- **No frontend changes in this change**

## Non-goals

- Authentication / authorization (no login, sessions, or JWT in this change)
- `PUT`/`PATCH`/`DELETE` user endpoints
- Password storage or any credential management
- Frontend user registration or profile UI
