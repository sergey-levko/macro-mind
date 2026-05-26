## Why

Authenticated users have no way to change their password after registration. This is a baseline account security requirement — users need to be able to rotate credentials without contacting an admin.

## What Changes

- Add `PUT /api/v1/users/me/password` endpoint: accepts `currentPassword` and `newPassword`, verifies the current password via BCrypt, replaces the hash in the `users` table.
- Return `400` if `newPassword` fails validation (min 8 chars).
- Return `401` if `currentPassword` does not match the stored hash.
- No schema migration needed — `password_hash` column already exists on `users`.

## Non-goals

- Password reset via email / forgot-password flow (no email infrastructure exists yet).
- Admin-initiated password resets.
- Forced password expiry or rotation policies.

## Capabilities

### New Capabilities

- `user-password`: Allows an authenticated user to update their own password by providing their current password and a new one.

### Modified Capabilities

<!-- No existing spec-level requirements are changing. The user-auth and user-profile specs are unaffected at the requirements level. -->

## Impact

- **Backend**: `UserController`, `UserService` — new endpoint + service method. No new dependencies.
- **Database**: No migration; `password_hash VARCHAR(255)` already present on `users`.
- **API**: New endpoint `PUT /api/v1/users/me/password`.
- **Security**: Endpoint is protected by JWT; current password must be verified before update.
- **Frontend**: New "Change Password" form on the profile page.
