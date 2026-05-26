## Why

New users who register land directly on the dashboard with no nutritional goals set, so every AI feature (daily/weekly insights, coach advice, macro targets) is blind or non-functional until they discover the Profile page and manually trigger goal generation. Completing the "register → goals" flow closes this gap and makes the app immediately useful.

## What Changes

- Remove the stale `Register.tsx` page (pre-auth dead code, not routed anywhere)
- Add a `/onboarding` route with a post-registration welcome page that prompts the user to generate their AI nutritional goals
- After successful registration in `LoginPage`, redirect to `/onboarding` instead of `/dashboard`
- The onboarding page shows a welcome message, the user's profile summary (name, age, weight, height, goal), and a single "Generate my macros" CTA that calls the existing `POST /api/v1/goals/generate` endpoint
- On success, redirect to `/dashboard`; provide a "Skip for now" escape hatch that also goes to `/dashboard`
- Add `ProtectedRoute` guard to `/onboarding` so it requires authentication

## Capabilities

### New Capabilities

- `onboarding`: Post-registration screen guiding users to generate their initial AI nutritional goals before reaching the main app

### Modified Capabilities

- `user-auth`: After registration, the redirect target changes from `/dashboard` to `/onboarding`

## Impact

- **Frontend only** — no backend or DB changes
- **New page**: `frontend/src/pages/Onboarding.tsx`
- **Modified**: `frontend/src/pages/LoginPage.tsx` — change post-register `navigate('/dashboard')` → `navigate('/onboarding')`
- **Modified**: `frontend/src/App.tsx` — add `/onboarding` route (protected)
- **Deleted**: `frontend/src/pages/Register.tsx` (dead code, unused since JWT auth was introduced)
- **API used**: `POST /api/v1/goals/generate` (already exists)

## Non-goals

- Multi-step wizard (profile editing during onboarding — users already set profile at register time)
- Onboarding for existing users who already have goals
- Email verification flow
- Backend changes
