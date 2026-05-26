## Context

Currently every API request carries a plain `X-User-Id: <uuid>` header that the backend trusts unconditionally. There is no registration, no login, and no access control — any caller can impersonate any user. Adding real authentication requires: a credential store, a stateless identity token, a Spring Security filter chain, and a frontend gate that blocks unauthenticated access.

## Goals / Non-Goals

**Goals:**
- Register a new user (email + hashed password) and return a signed JWT
- Authenticate an existing user and return a signed JWT
- Validate the JWT on every request and derive `userId` from it
- Remove all `X-User-Id` header trust from the backend
- Guard the React SPA — unauthenticated users see a login/register screen

**Non-Goals:**
- OAuth / social login
- Email verification or password reset
- Refresh tokens or token rotation
- Role-based access control
- Session-based auth (cookies, server-side sessions)

## Decisions

### JWT over sessions
Stateless tokens fit the single-page app model — no session store needed, horizontally scalable from day one. The token carries the `userId` (UUID) as the `sub` claim.

**Alternative considered:** Spring Session with PostgreSQL-backed sessions. Rejected because it adds infrastructure complexity (session table, TTL cleanup) with no benefit at this scale.

### Spring Security filter chain
A `JwtAuthFilter extends OncePerRequestFilter` intercepts every request, validates the token, and populates `SecurityContextHolder` with a `UsernamePasswordAuthenticationToken` carrying the `userId`. Controllers and services call `SecurityContextHolder.getContext().getAuthentication().getName()` to get the authenticated user ID — replacing the current `@RequestHeader("X-User-Id")` pattern across all slices.

**Alternative considered:** A custom `HandlerInterceptor`. Rejected because Spring Security's filter chain is the idiomatic place for auth, gives us the permit-list (`/api/v1/auth/**`) for free, and integrates with future RBAC if needed.

### BCrypt for password hashing
Spring Security ships `BCryptPasswordEncoder`. It's the safe default — adaptive cost factor, built-in salt.

### HS256 JWT signed with a shared secret
Symmetric signing keeps the implementation simple (no key-pair management). The secret is injected via environment variable `JWT_SECRET`. Token TTL is 7 days.

**Alternative considered:** RS256 (asymmetric). Overkill for a single-service deployment with no token consumers outside this backend.

### Token storage: localStorage
The frontend stores the JWT in `localStorage` and attaches it as `Authorization: Bearer <token>` on every request. Simpler than HttpOnly cookies, acceptable given no SSR and no sensitive third-party scripts.

### Schema change: `password_hash` column on `users`
A Liquibase changeset adds `password_hash VARCHAR(255) NOT NULL DEFAULT ''` (the default is temporary to allow migration of existing rows, then dropped). Email gains a `UNIQUE` constraint.

### `user-profile` modification: creation moves to register
Currently a user row must pre-exist (seeded). After this change `POST /api/v1/auth/register` creates the `users` row. The profile edit endpoint (`PUT /api/v1/users/me`) remains unchanged.

## Risks / Trade-offs

- **Existing test data invalidated** → Integration tests that seed a user row must also set `password_hash` or be updated to register via the API. Mitigation: update test fixtures in the same PR.
- **`X-User-Id` header removal is a breaking change** → Any client or test that sends the header directly will stop working. Mitigation: remove all usages in the same change; the integration test suite is the only consumer.
- **localStorage XSS exposure** → Acceptable risk at this stage; can migrate to HttpOnly cookies in a future hardening pass.
- **7-day TTL with no revocation** → Logout is client-side only (clear token). Acceptable for current threat model; revocation list can be added later.

## Migration Plan

1. Add Liquibase changeset: `password_hash` column + `email UNIQUE` constraint
2. Add `auth` vertical slice (register, login, JWT filter, Spring Security config)
3. Remove `X-User-Id` from all controllers; replace with `SecurityContextHolder` lookup
4. Update all integration tests to authenticate first and pass Bearer token
5. Add frontend auth context, login/register page, `ProtectedRoute`, token attachment in API client
6. Deploy: existing users have empty `password_hash` — they must re-register (acceptable for dev/demo stage)

**Rollback:** Revert the changeset and the Spring Security config; restore `X-User-Id` handling.

## Open Questions

- None — scope is well-defined.
