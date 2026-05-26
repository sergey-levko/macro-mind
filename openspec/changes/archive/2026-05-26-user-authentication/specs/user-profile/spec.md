## MODIFIED Requirements

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

## REMOVED Requirements

### Requirement: Retrieve a user profile by ID
**Reason**: With JWT authentication, the authenticated user retrieves their own profile via `GET /api/v1/users/me` (derived from the token). Fetching arbitrary users by UUID is not needed and would be a security risk.
**Migration**: Use `GET /api/v1/users/me` with a valid Bearer token instead of `GET /api/v1/users/{id}`.
