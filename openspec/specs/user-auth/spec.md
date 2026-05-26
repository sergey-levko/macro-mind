### Requirement: User registration
The system SHALL allow a new user to register with a unique email address and a password. On success the system SHALL return a signed short-lived access token (15 min TTL), a long-lived opaque refresh token (30 day TTL), and the created user's profile.

#### Scenario: Successful registration
- **WHEN** a POST request is sent to `/api/v1/auth/register` with a valid email and password (min 8 chars)
- **THEN** the system creates a `users` row with a BCrypt-hashed password, creates a `refresh_tokens` row, and returns HTTP 201 with `{ accessToken, refreshToken, user: { id, email, name } }`

#### Scenario: Duplicate email
- **WHEN** a POST request is sent to `/api/v1/auth/register` with an email that already exists
- **THEN** the system returns HTTP 409 Conflict with an error message

#### Scenario: Invalid input
- **WHEN** a POST request is sent to `/api/v1/auth/register` with a missing email or password shorter than 8 characters
- **THEN** the system returns HTTP 400 Bad Request with validation details

### Requirement: User login
The system SHALL authenticate an existing user by email and password and return a short-lived access token and a long-lived refresh token.

#### Scenario: Successful login
- **WHEN** a POST request is sent to `/api/v1/auth/login` with a correct email and password
- **THEN** the system returns HTTP 200 with `{ accessToken, refreshToken, user: { id, email, name } }`

#### Scenario: Wrong password
- **WHEN** a POST request is sent to `/api/v1/auth/login` with a correct email but wrong password
- **THEN** the system returns HTTP 401 Unauthorized

#### Scenario: Unknown email
- **WHEN** a POST request is sent to `/api/v1/auth/login` with an email that does not exist
- **THEN** the system returns HTTP 401 Unauthorized (same response as wrong password to prevent user enumeration)

### Requirement: Token refresh
The system SHALL allow a client holding a valid refresh token to obtain a new access token and a rotated refresh token without re-entering credentials.

#### Scenario: Successful refresh
- **WHEN** a POST request is sent to `/api/v1/auth/refresh` with a valid, non-expired, non-revoked refresh token in the request body `{ refreshToken }`
- **THEN** the system revokes the provided refresh token, issues a new refresh token, and returns HTTP 200 with `{ accessToken, refreshToken }`

#### Scenario: Expired refresh token
- **WHEN** a POST request is sent to `/api/v1/auth/refresh` with a refresh token whose `expires_at` is in the past
- **THEN** the system returns HTTP 401 Unauthorized

#### Scenario: Revoked refresh token
- **WHEN** a POST request is sent to `/api/v1/auth/refresh` with a refresh token that has `revoked = true`
- **THEN** the system returns HTTP 401 Unauthorized

#### Scenario: Unknown refresh token
- **WHEN** a POST request is sent to `/api/v1/auth/refresh` with a token that does not match any stored hash
- **THEN** the system returns HTTP 401 Unauthorized

### Requirement: Logout
The system SHALL allow an authenticated user to invalidate their current refresh token, preventing further silent refreshes.

#### Scenario: Successful logout
- **WHEN** a POST request is sent to `/api/v1/auth/logout` with a valid `Authorization: Bearer <accessToken>` header and `{ refreshToken }` in the body
- **THEN** the system marks the matching `refresh_tokens` row as `revoked = true` and returns HTTP 204 No Content

#### Scenario: Logout with unknown refresh token
- **WHEN** a POST request is sent to `/api/v1/auth/logout` with a refresh token not found in the database
- **THEN** the system returns HTTP 204 No Content (idempotent — already effectively logged out)

#### Scenario: Logout without authentication
- **WHEN** a POST request is sent to `/api/v1/auth/logout` with no or invalid `Authorization` header
- **THEN** the system returns HTTP 401 Unauthorized

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
