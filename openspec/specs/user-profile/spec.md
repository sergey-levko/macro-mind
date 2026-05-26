### Requirement: Register a new user
The system SHALL accept a registration request via the auth endpoint and persist a new user record alongside the hashed password, returning the created user's UUID, full profile, and a signed JWT. Direct user creation via `POST /api/v1/users` is no longer supported for registration — that path is replaced by `POST /api/v1/auth/register`.

#### Scenario: Successful registration with valid data
- **WHEN** `POST /api/v1/auth/register` is called with a valid JSON body containing `name`, `email`, `password` (min 8 chars), `age`, `weight_kg`, `height_cm`, and `goal_type`
- **THEN** the system persists the user with a BCrypt-hashed password, returns HTTP 201 with `{ token, user: { id, name, email, age, weight_kg, height_cm, goal_type } }`

#### Scenario: Registration fails when email is already taken
- **WHEN** `POST /api/v1/auth/register` is called with an `email` that already exists in the `users` table
- **THEN** the system returns HTTP 409 Conflict with an error message indicating the email is already registered

#### Scenario: Registration fails when required fields are missing
- **WHEN** `POST /api/v1/auth/register` is called with one or more required fields absent or blank
- **THEN** the system returns HTTP 400 Bad Request with a validation error listing the missing fields

#### Scenario: Registration fails when goal_type is invalid
- **WHEN** `POST /api/v1/auth/register` is called with a `goal_type` value not in `[LOSE_WEIGHT, MAINTAIN_WEIGHT, GAIN_MUSCLE]`
- **THEN** the system returns HTTP 400 Bad Request

### Requirement: Users table persisted via Liquibase
The `users` table SHALL be created exclusively through a Liquibase changeset; no raw DDL or `ddl-auto: create` is permitted.

#### Scenario: Schema applied on first startup
- **WHEN** the backend starts against a fresh PostgreSQL instance with no existing schema
- **THEN** Liquibase applies changeset `0002-users` and the `users` table exists with all required columns and constraints

#### Scenario: Changeset is idempotent on restart
- **WHEN** the backend restarts against a PostgreSQL instance where changeset `0002-users` was already applied
- **THEN** Liquibase skips the changeset and the application starts without errors

### Requirement: Update user profile
The system SHALL allow a user to update their profile fields via a PUT request.

#### Scenario: Successful profile update
- **WHEN** `PUT /api/v1/users/{id}` is called with a valid UUID and a JSON body containing `name`, `age`, `weight_kg`, `height_cm`, and `goal_type`
- **THEN** the system persists the updated values and returns HTTP 200 with the full profile: `id`, `name`, `email`, `age`, `weight_kg`, `height_cm`, and `goal_type`

#### Scenario: Update fails when user does not exist
- **WHEN** `PUT /api/v1/users/{id}` is called with a UUID that does not exist in the `users` table
- **THEN** the system returns HTTP 404 Not Found

#### Scenario: Update fails when required fields are missing or invalid
- **WHEN** `PUT /api/v1/users/{id}` is called with one or more required fields absent, blank, or out of valid range
- **THEN** the system returns HTTP 400 Bad Request with a validation error

#### Scenario: Update fails when goal_type is invalid
- **WHEN** `PUT /api/v1/users/{id}` is called with a `goal_type` value not in `[LOSE_WEIGHT, MAINTAIN_WEIGHT, GAIN_MUSCLE]`
- **THEN** the system returns HTTP 400 Bad Request
