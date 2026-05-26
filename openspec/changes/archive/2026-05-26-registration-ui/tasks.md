## 1. Cleanup

- [x] 1.1 Delete `frontend/src/pages/Register.tsx` (stale pre-auth page, not routed)

## 2. Onboarding Page

- [x] 2.1 Create `frontend/src/pages/Onboarding.tsx`: welcome message with user's name and goal type summary; "Generate my macros" primary button; "Skip for now" link; calls `POST /api/v1/goals/generate` via `api.post`; shows spinner ("Calculating your macros…") during request; on success redirects to `/dashboard`; on error shows inline error with "Try again" button
- [x] 2.2 Add `/onboarding` route to `frontend/src/App.tsx` inside the `ProtectedRoute` wrapper (same level as `/dashboard`, `/profile`, etc.)

## 3. Registration Redirect

- [x] 3.1 Update `LoginPage.tsx` register handler: change `navigate('/dashboard', { replace: true })` → `navigate('/onboarding', { replace: true })` after successful registration
