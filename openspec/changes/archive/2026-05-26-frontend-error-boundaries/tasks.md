## 1. Dependency

- [x] 1.1 Add `react-error-boundary` to `frontend/package.json` and install it

## 2. Fallback Components

- [x] 2.1 Create `frontend/src/components/RouteErrorFallback.tsx`: dark-themed inline fallback card with "Something went wrong" heading, error message (dev-only), "Reload section" reset button (`resetErrorBoundary` prop), and "Go to dashboard" link
- [x] 2.2 Create `frontend/src/components/RootErrorFallback.tsx`: full-page dark fallback (centred, matches login page style) with "Something went wrong" and a "Reload page" button (`window.location.reload()`)

## 3. Wire Up Boundaries

- [x] 3.1 Wrap each per-route element in `App.tsx` with `<ErrorBoundary FallbackComponent={RouteErrorFallback} resetKeys={[location.pathname]}>` — applies to: Dashboard, MealLog, Coach, Foods, Profile, and Onboarding
- [x] 3.2 Add a root-level `<ErrorBoundary FallbackComponent={RootErrorFallback}>` wrapping the entire `<BrowserRouter>` tree in `App.tsx`

## 4. TypeScript Check

- [x] 4.1 Run `npx tsc --noEmit` in `frontend/` and fix any type errors
