## Context

`LoginPage.tsx` already contains a working register tab that calls `POST /api/v1/auth/register` and receives both tokens. After registration, users are immediately redirected to `/dashboard` — which is effectively empty for new users: no nutritional goals means the macro-ring shows nothing, the Coach tab has no targets to analyze, and the daily summary shows zeros. The user must independently navigate to Profile and manually trigger AI goal generation.

A stale `Register.tsx` page also exists from the pre-auth era; it calls the old `POST /api/v1/users` endpoint and uses `UserContext` — it is no longer routed and can be deleted.

## Goals / Non-Goals

**Goals:**
- Guide new users to generate their AI nutritional goals immediately after registration, before they see the dashboard
- Provide a "Skip for now" escape to avoid blocking users who want to explore first
- Remove dead `Register.tsx` code

**Non-Goals:**
- Profile editing during onboarding (profile was already captured at registration)
- Detecting existing users who lack goals and redirecting them (future scope)
- Backend changes
- Multi-step wizard or animated walkthrough

## Decisions

### Decision 1 — Dedicated `/onboarding` route over a modal or dashboard banner

**Chosen:** A separate full-page `/onboarding` route, protected by `ProtectedRoute`, that the register flow redirects to.

**Why:** A modal on the dashboard requires the dashboard to load first (which looks broken for new users with no data). A banner is easy to dismiss before goals are generated. A dedicated page signals clearly that one step remains before the app is useful, and keeps the dashboard clean.

**Alternative considered:** Banner/toast on the dashboard. Rejected: too easy to miss; dashboard with no data looks broken.

### Decision 2 — Reuse existing `POST /api/v1/goals/generate` endpoint

**Chosen:** The onboarding page calls the same endpoint the Profile page already uses for AI goal generation.

**Why:** No backend change needed. The endpoint uses the authenticated user's profile (already saved at register time) as AI context — exactly the right data source.

### Decision 3 — No persistence of "onboarding completed" state

**Chosen:** The `/onboarding` route is only reached via the register redirect. No flag is stored; navigating directly to `/onboarding` after having goals is harmless (user can generate again or skip).

**Why:** Storing onboarding state (localStorage flag or DB column) adds complexity and a migration. For a single-user app the cost of "accidentally re-generating goals" is trivial.

**Alternative considered:** `localStorage` flag to redirect away if already completed. Rejected: premature for this scope.

## Risks / Trade-offs

- **AI goal generation can be slow (~2–3 s)** → Show a spinner with a friendly message ("Calculating your macros…"). The skip button remains visible during loading so the user is never stuck.
- **Goal generation can fail** → Show an inline error with a retry button; the "Skip for now" link is always available.
- **Users who skip will still see a blank dashboard** → Acceptable; the Profile page already handles goal generation for these users.
