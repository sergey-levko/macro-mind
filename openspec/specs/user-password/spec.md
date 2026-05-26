## ADDED Requirements

### Requirement: Authenticated user can update their own password
The system SHALL allow an authenticated user to change their password by providing their current password and a new password. The current password MUST be verified against the stored BCrypt hash before the update is applied.

#### Scenario: Successful password update
- **WHEN** `PUT /api/v1/users/me/password` is called with a valid JWT, a correct `currentPassword`, and a `newPassword` of at least 8 characters
- **THEN** the system replaces the stored password hash with a BCrypt hash of `newPassword` and returns HTTP 204 No Content

#### Scenario: Wrong current password
- **WHEN** `PUT /api/v1/users/me/password` is called with a valid JWT but a `currentPassword` that does not match the stored hash
- **THEN** the system returns HTTP 401 Unauthorized without modifying the stored password

#### Scenario: New password too short
- **WHEN** `PUT /api/v1/users/me/password` is called with a `newPassword` shorter than 8 characters
- **THEN** the system returns HTTP 400 Bad Request with a validation error describing the minimum length requirement

#### Scenario: Missing required fields
- **WHEN** `PUT /api/v1/users/me/password` is called with either `currentPassword` or `newPassword` absent or blank
- **THEN** the system returns HTTP 400 Bad Request with a validation error listing the missing fields

#### Scenario: Unauthenticated request
- **WHEN** `PUT /api/v1/users/me/password` is called without an `Authorization` header
- **THEN** the system returns HTTP 401 Unauthorized
