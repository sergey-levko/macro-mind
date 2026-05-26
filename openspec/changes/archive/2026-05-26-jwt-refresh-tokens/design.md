## Context

The current `JwtService` issues a single token with a 7-day TTL. There is no logout endpoint and no way to revoke a token server-side. On the frontend, `localStorage` holds one key (`token`); when it expires the user is silently bounced to login with no recovery path. This change introduces the standard short-lived access token + long-lived refresh token pattern.

## Goals / Non-Goals

**Goals:**
- Reduce access token exposure window to 15 minutes
- Enable server-side logout via refresh token revocation
- Provide transparent silent refresh so users stay logged in for 30 days without manual re-auth
- Maintain backward-compatible public API shape (just adds fields)

**Non-Goals:**
- Per-device session listing or selective revocation
- Refresh token rotation on every API call (only on `/refresh`)
- HttpOnly cookie transport (out of scope for this SPA architecture)
- Access token blacklisting

## Decisions

### Decision 1 — Stateful (DB-stored) refresh tokens over stateless JWT refresh tokens

**Chosen:** Store an opaque, randomly-generated refresh token whose SHA-256 hash is persisted in a `refresh_tokens` table.

**Why:** Stateless JWT refresh tokens cannot be revoked server-side — a stolen long-lived JWT refresh token is as bad as the current 7-day access token. Stateful tokens allow logout to immediately invalidate the session.

**Alternative considered:** JWT refresh token (stateless). Rejected: no revocation, defeats the purpose of this change.

### Decision 2 — Refresh token stored in `localStorage` (not httpOnly cookie)

**Chosen:** Return the refresh token in the JSON response body; frontend stores in `localStorage` alongside the access token.

**Why:** The app has no server-side rendering or cookie infrastructure. Introducing httpOnly cookies requires CORS `credentials: include`, CSRF protection, and `Set-Cookie` headers — a large scope increase. `localStorage` is acceptable given XSS is mitigated by React's default escaping and the absence of untrusted script injection.

**Alternative considered:** httpOnly cookie. Preferred security-wise but out of scope for this iteration.

### Decision 3 — Token rotation on `/refresh` only (not on every request)

**Chosen:** Issue a new access token + new refresh token (and revoke the old one) only when `/api/v1/auth/refresh` is called.

**Why:** Rotating on every API call requires the frontend to update the stored token on every response — complex, race-condition-prone, and unnecessary for a single-user SPA.

### Decision 4 — `refresh_tokens` table with `revoked` flag (soft delete)

**Chosen:** `refresh_tokens(id UUID PK, user_id UUID FK, token_hash VARCHAR(64), expires_at TIMESTAMP, created_at TIMESTAMP, revoked BOOLEAN DEFAULT false)`.

**Why:** Soft delete allows future auditing. The `revoked` flag is checked on every `/refresh` call. A nightly job (future work) can hard-delete expired rows.

**Alternative considered:** Delete row on logout (hard delete). Simpler but loses audit trail.

### Decision 5 — `AuthResponse` shape change: `token` → `accessToken` + `refreshToken`

**Chosen:** Rename the existing `token` field to `accessToken` and add `refreshToken` to `AuthResponse`.

**Why:** Clarity — callers need to distinguish which token to send as Bearer and which to send to `/refresh`. The rename is a breaking change for the frontend only (no external consumers).

### Decision 6 — Frontend 401 interceptor in `api.ts`

**Chosen:** Wrap the `api` client's request method: on 401, call `POST /api/v1/auth/refresh`, store new tokens, retry the original request once. If refresh fails, clear storage and redirect to login.

**Why:** Centralises the refresh logic in one place rather than duplicating across every page component.

## Risks / Trade-offs

- **Refresh token in localStorage is vulnerable to XSS** → Mitigated by React's DOM escaping and no eval/dangerouslySetInnerHTML usage in this codebase. Acceptable for this app's threat model.
- **Race condition: multiple concurrent 401s trigger multiple refresh calls** → Mitigation: track an in-flight refresh promise in `api.ts` and queue subsequent retries behind it rather than firing parallel refresh requests.
- **Old frontends (before this deploy) send `token` key from localStorage** → On first load, the interceptor will fail the refresh (no refresh token stored) and redirect to login — a one-time forced re-authentication. Acceptable.

## Migration Plan

1. Deploy backend with new `refresh_tokens` table (Liquibase changeset) — new endpoints active, old `/register` and `/login` now return both tokens.
2. Deploy frontend — new `api.ts` stores `accessToken` + `refreshToken`; old `token` key in localStorage is ignored.
3. Users are prompted to log in once (their stored 7-day token will still work as a Bearer token until it expires, but no refresh token exists → first 401 triggers login redirect).
4. No rollback complexity: the `refresh_tokens` table can remain empty on rollback; the old frontend just won't send refresh tokens.

## Open Questions

- Should logout revoke **all** refresh tokens for the user (all devices) or just the current one? → Current design: revoke only the token provided in the request. All-device logout is a non-goal.
- Cleanup job for expired rows in `refresh_tokens`? → Deferred; table will remain small for a single-user app.
