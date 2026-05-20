### Requirement: Register a new user
The system SHALL accept a registration request and persist a new user record, returning the created user's UUID and full profile.

#### Scenario: Successful registration with valid data
- **WHEN** `POST /api/v1/users` is called with a valid JSON body containing `name`, `email`, `age`, `weight_kg`, `height_cm`, and `goal_type`
- **THEN** the system persists the user, returns HTTP 201 with the created user's `id`, `name`, `email`, `age`, `weight_kg`, `height_cm`, and `goal_type`

#### Scenario: Registration fails when email is already taken
- **WHEN** `POST /api/v1/users` is called with an `email` that already exists in the `users` table
- **THEN** the system returns HTTP 409 Conflict with an error message indicating the email is already registered

#### Scenario: Registration fails when required fields are missing
- **WHEN** `POST /api/v1/users` is called with one or more required fields absent or blank
- **THEN** the system returns HTTP 400 Bad Request with a validation error listing the missing fields

#### Scenario: Registration fails when goal_type is invalid
- **WHEN** `POST /api/v1/users` is called with a `goal_type` value not in `[LOSE_WEIGHT, MAINTAIN_WEIGHT, GAIN_MUSCLE]`
- **THEN** the system returns HTTP 400 Bad Request

### Requirement: Retrieve a user profile by ID
The system SHALL return the full profile for a user identified by their UUID.

#### Scenario: Successful profile retrieval
- **WHEN** `GET /api/v1/users/{id}` is called with a valid UUID that exists in the `users` table
- **THEN** the system returns HTTP 200 with the user's `id`, `name`, `email`, `age`, `weight_kg`, `height_cm`, and `goal_type`

#### Scenario: Profile not found
- **WHEN** `GET /api/v1/users/{id}` is called with a UUID that does not exist in the `users` table
- **THEN** the system returns HTTP 404 Not Found

#### Scenario: Profile retrieval fails with invalid UUID format
- **WHEN** `GET /api/v1/users/{id}` is called with a string that is not a valid UUID
- **THEN** the system returns HTTP 400 Bad Request

### Requirement: Users table persisted via Liquibase
The `users` table SHALL be created exclusively through a Liquibase changeset; no raw DDL or `ddl-auto: create` is permitted.

#### Scenario: Schema applied on first startup
- **WHEN** the backend starts against a fresh PostgreSQL instance with no existing schema
- **THEN** Liquibase applies changeset `0002-users` and the `users` table exists with all required columns and constraints

#### Scenario: Changeset is idempotent on restart
- **WHEN** the backend restarts against a PostgreSQL instance where changeset `0002-users` was already applied
- **THEN** Liquibase skips the changeset and the application starts without errors
