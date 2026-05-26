### Requirement: User registration
The system SHALL allow a new user to register with a unique email address and a password. On success the system SHALL return a signed JWT and the created user's profile.

#### Scenario: Successful registration
- **WHEN** a POST request is sent to `/api/v1/auth/register` with a valid email and password (min 8 chars)
- **THEN** the system creates a `users` row with a BCrypt-hashed password, returns HTTP 201 with `{ token, user: { id, email, name } }`

#### Scenario: Duplicate email
- **WHEN** a POST request is sent to `/api/v1/auth/register` with an email that already exists
- **THEN** the system returns HTTP 409 Conflict with an error message

#### Scenario: Invalid input
- **WHEN** a POST request is sent to `/api/v1/auth/register` with a missing email or password shorter than 8 characters
- **THEN** the system returns HTTP 400 Bad Request with validation details

### Requirement: User login
The system SHALL authenticate an existing user by email and password and return a signed JWT.

#### Scenario: Successful login
- **WHEN** a POST request is sent to `/api/v1/auth/login` with a correct email and password
- **THEN** the system returns HTTP 200 with `{ token, user: { id, email, name } }`

#### Scenario: Wrong password
- **WHEN** a POST request is sent to `/api/v1/auth/login` with a correct email but wrong password
- **THEN** the system returns HTTP 401 Unauthorized

#### Scenario: Unknown email
- **WHEN** a POST request is sent to `/api/v1/auth/login` with an email that does not exist
- **THEN** the system returns HTTP 401 Unauthorized (same response as wrong password to prevent user enumeration)

### Requirement: JWT validation on protected endpoints
The system SHALL validate the JWT on every request to `/api/v1/**` except `/api/v1/auth/**`.

#### Scenario: Valid token
- **WHEN** a request includes `Authorization: Bearer <valid-token>` header
- **THEN** the system extracts `userId` from the token's `sub` claim and processes the request normally

#### Scenario: Missing token
- **WHEN** a request to a protected endpoint has no `Authorization` header
- **THEN** the system returns HTTP 401 Unauthorized

#### Scenario: Expired or malformed token
- **WHEN** a request includes an expired or malformed JWT
- **THEN** the system returns HTTP 401 Unauthorized

### Requirement: X-User-Id header removal
The system SHALL derive the authenticated user's identity exclusively from the JWT. The `X-User-Id` request header SHALL no longer be accepted or trusted by any endpoint.

#### Scenario: Request with only X-User-Id header
- **WHEN** a request to a protected endpoint sends `X-User-Id` but no `Authorization` header
- **THEN** the system returns HTTP 401 Unauthorized
