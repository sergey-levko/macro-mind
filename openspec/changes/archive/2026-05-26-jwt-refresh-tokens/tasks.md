## 1. Database

- [x] 1.1 Add Liquibase changeset to create `refresh_tokens` table (`id UUID PK`, `user_id UUID FK → users.id`, `token_hash VARCHAR(64) NOT NULL`, `expires_at TIMESTAMP NOT NULL`, `created_at TIMESTAMP NOT NULL`, `revoked BOOLEAN NOT NULL DEFAULT false`)

## 2. Backend — Refresh Token Infrastructure

- [x] 2.1 Create `RefreshToken` JPA entity and `RefreshTokenRepository` (`findByTokenHash`, `existsByTokenHashAndRevokedFalseAndExpiresAtAfter`)
- [x] 2.2 Add `RefreshTokenService`: `createRefreshToken(UUID userId)` — generates a `SecureRandom` token, stores its SHA-256 hash with 30-day expiry; `validateAndRotate(String rawToken)` — looks up hash, validates not-revoked + not-expired, revokes old row, returns new raw token + new `RefreshToken`; `revokeByRawToken(String rawToken)` — revokes matching row (no-op if not found)

## 3. Backend — Auth Changes

- [x] 3.1 Update `AuthResponse` record: rename field `token` → `accessToken`, add `refreshToken` field
- [x] 3.2 Reduce `JwtService.TTL_MS` from 7 days to 15 minutes
- [x] 3.3 Update `AuthService.register` and `AuthService.login` to call `RefreshTokenService.createRefreshToken` and return both tokens in `AuthResponse`
- [x] 3.4 Add `RefreshRequest` record (`String refreshToken`) and `RefreshResponse` record (`String accessToken`, `String refreshToken`)
- [x] 3.5 Implement `POST /api/v1/auth/refresh` in `AuthController` + `AuthService.refresh(String rawToken)` — delegates to `RefreshTokenService.validateAndRotate`, issues new access token, returns `RefreshResponse`
- [x] 3.6 Implement `POST /api/v1/auth/logout` (protected) in `AuthController` + `AuthService.logout(String rawToken)` — delegates to `RefreshTokenService.revokeByRawToken`, returns 204

## 4. Backend — Tests

- [x] 4.1 Unit tests for `RefreshTokenService`: `createRefreshToken` stores hashed token; `validateAndRotate` returns new tokens and revokes old; `validateAndRotate` throws on expired; `validateAndRotate` throws on revoked; `revokeByRawToken` is a no-op for unknown token
- [x] 4.2 Update `AuthServiceTest`: `login` and `register` return `AuthResponse` with both `accessToken` and `refreshToken`; `refresh` returns new tokens; `logout` delegates revocation
- [x] 4.3 Update `AuthControllerTest`: `POST /auth/login` response shape has `accessToken` + `refreshToken`; `POST /auth/refresh` with valid token returns 200; `POST /auth/refresh` with invalid token returns 401; `POST /auth/logout` returns 204; `POST /auth/logout` unauthenticated returns 401
- [x] 4.4 Update `AuthIntegrationTest`: full register → login → refresh → logout flow; verify refresh token is revoked after logout; verify rotated token from `/refresh` invalidates the previous one

## 5. Frontend

- [x] 5.1 Update `api.ts` token storage: change stored key from `token` to `accessToken`; add `refreshToken` key; update `getToken()` to read `accessToken`; update login/register handlers to store both tokens
- [x] 5.2 Add silent-refresh interceptor to `api.ts`: on 401, call `POST /api/v1/auth/refresh` with stored `refreshToken`; on success store new `accessToken` + `refreshToken` and retry the original request once; on refresh failure clear both tokens and redirect to `/login`; de-duplicate concurrent 401s with a single in-flight refresh promise
- [x] 5.3 Update logout handler (Profile page or wherever logout lives): call `POST /api/v1/auth/logout` with current `refreshToken` before clearing storage
