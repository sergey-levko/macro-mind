## Why

MacroMind has no login system — all requests use a hardcoded `X-User-Id` header, meaning any user can access any data and there is no way to create or own an account. Adding real authentication gates every API endpoint behind a verified identity and enables multi-user production use.

## What Changes

- Users can register with email + password and receive a JWT access token
- Users can log in with email + password and receive a JWT access token
- All existing `/api/v1/*` endpoints require a valid JWT (no more hardcoded `X-User-Id`)
- The frontend stores the token and attaches it to every API request
- A login/register screen replaces the app when no valid token is present
- Logout clears the token and returns to the login screen

## Capabilities

### New Capabilities
- `user-auth`: Registration, login, logout, and JWT issuance/validation
- `auth-guard`: Frontend route protection — redirect unauthenticated users to login

### Modified Capabilities
- `user-profile`: User creation now happens at registration (POST /api/v1/auth/register), not as a separate step; `user-id` is derived from the JWT, not from a request header

## Impact

- **Database**: `users` table gains `password_hash` column (Liquibase changeset)
- **New API endpoints**: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`
- **Modified API endpoints**: all existing `/api/v1/*` — `X-User-Id` header replaced by JWT extraction in a Spring Security filter
- **Dependencies added**: Spring Security, `jjwt` (JWT library) on the backend; token stored in `localStorage` on the frontend
- **Non-goals**: OAuth / social login, email verification, password reset, refresh tokens, role-based access control
