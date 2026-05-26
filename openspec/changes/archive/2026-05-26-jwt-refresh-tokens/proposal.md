## Why

The current implementation issues 7-day access tokens with no logout endpoint and no revocation mechanism — a stolen token grants full account access for up to a week with no remedy. Introducing short-lived access tokens (15 min) paired with long-lived, DB-backed refresh tokens (30 days) reduces the attack window, enables real logout, and lets users stay signed in across sessions without manual re-authentication.

## What Changes

- Access token TTL reduced from 7 days to 15 minutes
- New `refresh_tokens` DB table stores hashed refresh tokens with expiry and revoked flag
- `POST /api/v1/auth/register` and `POST /api/v1/auth/login` return both `accessToken` and `refreshToken` (**BREAKING**: `AuthResponse.token` field renamed to `accessToken`)
- New `POST /api/v1/auth/refresh` — exchanges a valid refresh token for a new access token + rotated refresh token
- New `POST /api/v1/auth/logout` (authenticated) — revokes the current refresh token
- Frontend: stores both tokens, adds a 401-interceptor that silently calls `/refresh` and retries the original request; clears both tokens on logout

## Capabilities

### New Capabilities

*(none — this change extends existing auth behavior)*

### Modified Capabilities

- `user-auth`: New endpoints (`/refresh`, `/logout`), changed `AuthResponse` shape (adds `refreshToken`, renames `token` → `accessToken`), reduced access token TTL, new DB-backed refresh token lifecycle

## Impact

- **DB**: New `refresh_tokens` table (`id`, `user_id FK`, `token_hash`, `expires_at`, `created_at`, `revoked`) — Liquibase changeset required
- **API**: `POST /api/v1/auth/register`, `POST /api/v1/auth/login` — response shape changes; new `POST /api/v1/auth/refresh` (public), `POST /api/v1/auth/logout` (protected)
- **Frontend**: `api.ts` token storage key changes; new silent-refresh interceptor; logout clears both tokens
- **Tests**: `AuthControllerTest`, `AuthServiceTest`, `AuthIntegrationTest` need updates

## Non-goals

- Refresh token rotation on every API call (rotation only on `/refresh`)
- Multi-device session management or per-device token listing
- OAuth / third-party login flows
- Token blacklisting for access tokens (short TTL makes this unnecessary)
