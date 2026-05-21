## Why

The backend exposes a complete REST API across 7 capabilities, but the frontend is a single placeholder splash screen with no routing, pages, or API integration. Users have no way to interact with the product. This change builds the foundational frontend shell — routing, layout, user context, and the two highest-value feature pages (Dashboard and Meal Logging) — establishing the pattern all subsequent frontend slices will follow.

## What Changes

- Replace the placeholder `App.tsx` with a real routing tree (React Router)
- Add a persistent layout with navigation sidebar/topbar
- Add user registration and a `userId` context stored in `localStorage` (no auth — the backend uses `X-User-Id` header identity)
- Add a central API client (`src/lib/api.ts`) that injects `X-User-Id` on every request, proxy-forwarded to `http://localhost:8080`
- Implement **Dashboard page** — summary card (today's macros vs targets), daily macro bar chart, weekly macro chart (Recharts)
- Implement **Meal Logging page** — list today's meals by type, add/delete meal logs, search and add food items to a meal log
- Add a **Nutritional Goals widget** (inline on Dashboard) so users can set targets they see charted immediately
- Add `react-router-dom` as a dependency

## Capabilities

### New Capabilities
- `frontend-shell`: App-level shell — routing, layout, navigation, user registration flow, API client with `X-User-Id` injection
- `frontend-dashboard`: Dashboard page with summary card, macro percentages, daily and weekly Recharts visualisations, inline goal-setting
- `frontend-meal-logging`: Meal logging page — view today's meals by type, create/delete meal logs, add/remove food items with live macro totals

### Modified Capabilities
<!-- none -->

## Impact

- `frontend/src/` — effectively replaced (routing, pages, components, API client)
- No backend changes, no DB changes, no new API endpoints
- Adds `react-router-dom` npm dependency
- Consumes: `/api/v1/users`, `/api/v1/dashboard/*`, `/api/v1/meal-logs`, `/api/v1/meal-logs/{id}/items`, `/api/v1/foods`, `/api/v1/nutritional-goals`

## Non-goals

- No authentication or session tokens (identity is UUID in localStorage)
- No USDA food import UI (food search covers manual + existing foods only)
- No AI advice page (future change)
- No food catalog management page (future change)
- No responsive/mobile layout optimisation
