## Context

MacroMind already has full JWT-based authentication (BCrypt password hashing, `JwtService`, `JwtAuthFilter`). The `users` table has a `password_hash VARCHAR(255)` column. The `UserService` and `UserController` handle profile reads/updates. No external dependencies are needed — BCrypt is already on the classpath via `spring-boot-starter-security`.

The missing piece is a dedicated endpoint that lets an authenticated user replace their own password after verifying their current one.

## Goals / Non-Goals

**Goals:**
- Add `PUT /api/v1/users/me/password` that verifies `currentPassword` against the stored BCrypt hash and replaces it with a hash of `newPassword`.
- Return `401` on wrong current password, `400` on validation failure, `204` on success.
- Cover the endpoint with unit tests (service) and an integration test (Testcontainers).

**Non-Goals:**
- Forgot-password / email-based reset flow.
- Admin password override.
- Password strength rules beyond minimum 8-character length.
- Token invalidation after password change (existing JWTs remain valid).

## Decisions

### 1. Response body: 204 No Content

`PUT /api/v1/users/me/password` returns `204` on success with no body. Returning a new JWT was considered but rejected — there is no security requirement to invalidate existing sessions at this stage, and it would complicate the frontend unnecessarily.

### 2. Reuse `UserService` — no new service class

The password update logic (BCrypt verify → BCrypt encode → save) is 3 lines and belongs in `UserService.updatePassword(UUID userId, String currentPassword, String newPassword)`. Creating a separate `PasswordService` would be over-engineering for this scope.

### 3. Error distinction: `currentPassword` wrong → `401`, not `400`

Wrong current password is an authentication failure, not a validation error. Using `401` is consistent with the login endpoint's behavior and allows the frontend to distinguish "bad credentials" from "bad input".

### 4. No Liquibase changeset

`password_hash` already exists on the `users` table (added in `0008-auth.yaml`). No schema migration is needed.

### 5. Frontend: inline form on Profile page

A "Change Password" section is added to the existing `Profile.tsx` page. A separate route was considered but rejected — the operation is tightly related to account settings and doesn't warrant its own page.

## Risks / Trade-offs

- **Brute-force on current password** → The endpoint is JWT-protected (attacker must already have a valid token). Rate limiting is a future concern, not in scope here.
- **No session invalidation after change** → If a token is stolen and the user changes their password, the stolen token remains valid until it expires (7-day TTL). Acceptable for the current threat model; can be revisited when a token-revocation list is introduced.
