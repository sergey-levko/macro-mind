## Why

Any unhandled render error (thrown during React's render phase) tears down the entire React tree and leaves the user on a blank white screen with no way to recover except a manual page reload. The `.map is not a function` crash on the Meal Log page that was just fixed demonstrates the problem: a single bad API response shape brought down the whole app — nav, other tabs, everything. React error boundaries isolate failures to the affected subtree and replace it with a recoverable fallback UI.

## What Changes

- Add a reusable `ErrorBoundary` fallback component with a dark-themed "Something went wrong" card, a "Try reloading this section" reset button, and a "Go to dashboard" escape link
- Wrap each top-level route inside the `ProtectedRoute` layout with its own error boundary, so a crash on one page (e.g. Meal Log) doesn't affect the nav or other tabs
- Add a root-level boundary in `App.tsx` as a last-resort catch-all for errors outside individual routes (e.g. the `/onboarding` page or the Layout itself)
- Add `react-error-boundary` dependency (well-maintained, typed, avoids boilerplate class component)

## Capabilities

### New Capabilities

- `frontend-error-recovery`: React error boundaries that catch render-phase errors per route and display a recovery UI instead of crashing the whole page.

### Modified Capabilities

*(none — no API contracts or backend behavior changes)*

## Impact

- **`frontend/package.json`** — add `react-error-boundary`
- **`frontend/src/components/RouteErrorFallback.tsx`** — new fallback UI component
- **`frontend/src/App.tsx`** — wrap each route element with `<ErrorBoundary>`
- No backend changes, no database tables affected, no API endpoints added or modified
