## Context

React error boundaries are class components (or wrappers) that implement `componentDidCatch` / `getDerivedStateFromError`. When a child throws during render, React walks up the tree to the nearest boundary and renders its fallback instead of unmounting everything above it. Without any boundaries, the error propagates to the root and the entire app tree is unmounted.

The current app has no error boundaries at any level. The router tree is:

```
<BrowserRouter>
  <AuthProvider>
    <ToastProvider>
      <Routes>
        /login            → LoginPage (unprotected)
        /onboarding       → ProtectedRoute > Onboarding
        /                 → ProtectedRoute > Layout > Outlet
          /dashboard      → Dashboard
          /meal-log       → MealLog
          /coach          → Coach
          /foods          → Foods
          /profile        → Profile
      </Routes>
    </ToastProvider>
  </AuthProvider>
</BrowserRouter>
```

A crash in `MealLog` today unmounts everything including `Layout` and the nav sidebar.

## Goals / Non-Goals

**Goals:**
- Isolate render crashes to the individual route that caused them
- Give users a recoverable fallback (reset button + nav escape) instead of a blank page
- Protect the root app tree with a last-resort boundary

**Non-Goals:**
- Error reporting / remote logging (e.g. Sentry) — future scope
- Boundaries inside individual page components (component-level granularity)
- Catching async errors (network failures, promise rejections) — those are already handled via try/catch in each page

## Decisions

### Decision 1 — `react-error-boundary` package over a custom class component

**Chosen:** Use the `react-error-boundary` npm package (`ErrorBoundary` + `FallbackComponent` prop).

**Why:** Writing a class component for error boundaries involves lifecycle boilerplate (`getDerivedStateFromError`, `componentDidCatch`, `state.hasError`, `reset` callback). `react-error-boundary` wraps this correctly, is TypeScript-native, provides a `resetKeys` prop for automatic reset on navigation, and is the de-facto standard for this pattern. It has no transitive dependencies.

**Alternative considered:** Custom `class ErrorBoundary extends React.Component`. Rejected: more code, easy to get the reset logic wrong, no `resetKeys` support.

### Decision 2 — Per-route boundaries, not a single app-level boundary

**Chosen:** Wrap each individual route `element` with its own `<ErrorBoundary>`, plus one root-level boundary in `App.tsx`.

**Why:** A single boundary at the root catches everything but replaces the entire UI including the nav sidebar. With per-route boundaries, a crash on `/meal-log` shows the fallback only in the `<main>` content area while the nav and Layout remain intact — the user can navigate to another tab without a page reload.

**Alternative considered:** Single boundary wrapping `<Routes>`. Rejected: collapses the nav on any crash.

### Decision 3 — `resetKeys={[location.pathname]}` for automatic boundary reset on navigation

**Chosen:** Pass the current `location.pathname` as a `resetKey` to each per-route boundary. When the user navigates away and back, the boundary resets automatically.

**Why:** Without `resetKeys`, once a boundary catches an error it stays in the error state forever until the component is remounted. This would mean the Meal Log stays broken even after navigating away and returning. `resetKeys` is a built-in feature of `react-error-boundary` that calls `reset()` whenever a key changes.

### Decision 4 — Fallback UI: dark-themed inline card, "Reload section" + "Go to dashboard"

**Chosen:** A `RouteErrorFallback` component styled to match the app's dark theme (`bg-gray-900`, `text-white`), with two actions: reset the boundary (re-render the route) and navigate to `/dashboard` as an escape hatch.

**Why:** Matches the existing visual language. Two actions cover the two most common recovery paths: "try again in place" and "get me out of here".

## Risks / Trade-offs

- **`react-error-boundary` only catches render-phase errors** — async errors (API call failures, unhandled promise rejections) are not caught. This is expected React behaviour; async errors should continue to be handled with try/catch inside components.
- **Boundaries do not reset on the same route** — if the user clicks "Reload section" and the same bug triggers again (e.g. deterministic bad data), they'll see the error boundary again. This is correct behaviour; the underlying data issue must be fixed.
- **LoginPage and Onboarding are not inside Layout** — they need their own boundary or the root boundary covers them. The root boundary in `App.tsx` is sufficient as a last resort.
