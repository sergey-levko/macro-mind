## Context

The frontend is a Vite + React 19 + TypeScript + TailwindCSS scaffold with Recharts already installed. It has one placeholder component and no routing, no API calls, and no state management. The backend is a complete Spring Boot API on port 8080 with a Vite dev proxy already wired at `/api` → `http://localhost:8080`. The backend uses `X-User-Id` (UUID) header for identity on every request — there is no JWT or session auth.

## Goals / Non-Goals

**Goals:**
- Establish the folder structure, routing, and layout shell all future slices will follow
- Implement user registration and UUID-based identity via localStorage
- Build a central API client that injects `X-User-Id` on every request
- Build the Dashboard page (summary card, macro percentage rings, weekly bar chart)
- Build the Meal Logging page (today's meals by type, add/delete meals, add/remove food items)
- Inline goal-setting widget on Dashboard so targets appear immediately on the chart

**Non-Goals:**
- Authentication, session tokens, JWT
- AI advice page
- Food catalog management page
- USDA food import UI
- Mobile/responsive layout
- Unit or integration tests (UI smoke testing by running the dev server)

## Decisions

### 1. Routing: React Router v7 (react-router-dom)
React Router is the standard for Vite+React SPAs. TanStack Router is an alternative but adds TypeScript complexity not yet needed. Install `react-router-dom@^7`.

### 2. No external state management library
Server state is fetched per-page with `useEffect` + `useState`. This avoids adding React Query/Zustand before patterns are established. If data-fetching complexity grows, TanStack Query can be added in a later change.

### 3. User identity: localStorage UUID
The backend requires `X-User-Id: <uuid>` on every request. The app stores this UUID in `localStorage` under the key `macromind_user_id`. On first visit (no key present), the app redirects to a `/register` page that calls `POST /api/v1/users` and stores the returned UUID. All subsequent sessions read the UUID from localStorage. No logout concept.

### 4. API client: thin fetch wrapper (`src/lib/api.ts`)
A minimal typed wrapper around `fetch` that:
- Reads `userId` from localStorage and sets `X-User-Id` header
- Sets `Content-Type: application/json`
- Returns typed JSON or throws on non-2xx
No axios dependency needed.

### 5. Folder structure (feature-based slices)
```
src/
  lib/
    api.ts          # fetch wrapper
    types.ts        # shared TS types mirroring backend DTOs
  context/
    UserContext.tsx # React context for userId + user profile
  components/
    Layout.tsx      # sidebar + outlet
    NavLink.tsx
  pages/
    Register.tsx
    Dashboard.tsx
    MealLog.tsx
```
Each page owns its own data fetching; no shared store.

### 6. Dashboard charts: Recharts (already installed)
- Summary card: four macro progress bars (calories, protein, carbs, fat) showing `pct` from `/api/v1/dashboard/summary`
- Weekly chart: `BarChart` with 7 days from `/api/v1/dashboard/weekly`, one bar per day (calories)

### 7. Meal Logging: accordion by meal type
Today's meals grouped by `BREAKFAST | LUNCH | DINNER | SNACK`. Each group shows meal logs; each log is expandable to show its items with macro totals. Add buttons open inline forms.

### 8. Food search for adding items
When adding a food item to a meal, a search input calls `GET /api/v1/foods?search=<term>` (debounced 300ms). Results are shown in a dropdown; selecting one sets the foodId and lets the user enter quantity (grams).

## Risks / Trade-offs

- **No optimistic updates** → short loading states visible on add/delete. Acceptable for a dev-phase product.
- **localStorage UUID is not secure** → any script on the page can read it. Not a concern since there is no auth layer anyway.
- **No error boundary** → fetch failures surface as empty states or console errors. Add in a later change.
- **Recharts bundle size** → already a dependency, acceptable.
