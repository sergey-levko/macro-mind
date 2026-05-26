## 1. Backend — Service Layer

- [x] 1.1 Add `UpdatePasswordRequest` record to `user` package: `currentPassword` (not blank) and `newPassword` (not blank, min 8 chars) with Bean Validation annotations
- [x] 1.2 Add `updatePassword(UUID userId, String currentPassword, String newPassword)` to `UserService`: load user, verify `currentPassword` via `PasswordEncoder.matches`, throw `InvalidCredentialsException` on mismatch, encode and save new hash
- [x] 1.3 Add unit tests for `UserService.updatePassword`: success path, wrong current password (expect `InvalidCredentialsException`), new password saved as BCrypt hash

## 2. Backend — Controller & Error Handling

- [x] 2.1 Add `PUT /api/v1/users/me/password` to `UserController`: accept `@Valid @RequestBody UpdatePasswordRequest`, delegate to `UserService.updatePassword`, return `ResponseEntity.noContent()`
- [x] 2.2 Ensure `GlobalExceptionHandler` maps `InvalidCredentialsException` to `401` (already handles auth errors — verify it covers this case, add if missing)

## 3. Backend — Integration Test

- [x] 3.1 Add integration tests in `UserIntegrationTest`: successful password update returns 204, subsequent login with new password succeeds; wrong current password returns 401; new password too short returns 400; unauthenticated request returns 401

## 4. Frontend — Change Password Form

- [x] 4.1 Add a "Change Password" section to `Profile.tsx`: form with `currentPassword`, `newPassword`, and `confirmNewPassword` fields; validate that new passwords match client-side; call `PUT /api/v1/users/me/password`
- [x] 4.2 Handle API responses on the form: show success message on 204, show "Current password is incorrect" on 401, show validation errors on 400
