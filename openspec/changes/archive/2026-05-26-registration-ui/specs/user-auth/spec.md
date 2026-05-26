## MODIFIED Requirements

### Requirement: User registration
The system SHALL allow a new user to register with a unique email address and a password. On success the system SHALL return a signed short-lived access token (15 min TTL), a long-lived opaque refresh token (30 day TTL), and the created user's profile. After successful registration the client SHALL redirect to `/onboarding` (not `/dashboard`).

#### Scenario: Successful registration
- **WHEN** a POST request is sent to `/api/v1/auth/register` with a valid email and password (min 8 chars)
- **THEN** the system creates a `users` row with a BCrypt-hashed password, creates a `refresh_tokens` row, and returns HTTP 201 with `{ accessToken, refreshToken, user: { id, email, name } }`
- **THEN** the frontend stores both tokens and navigates to `/onboarding`

#### Scenario: Duplicate email
- **WHEN** a POST request is sent to `/api/v1/auth/register` with an email that already exists
- **THEN** the system returns HTTP 409 Conflict with an error message

#### Scenario: Invalid input
- **WHEN** a POST request is sent to `/api/v1/auth/register` with a missing email or password shorter than 8 characters
- **THEN** the system returns HTTP 400 Bad Request with validation details
