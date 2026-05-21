## 1. Project Setup

- [x] 1.1 Create branch `feat/frontend-core`, install `react-router-dom@^7`, update `index.html` title to "MacroMind"
- [x] 1.2 Create `src/lib/types.ts` with TypeScript interfaces mirroring all backend DTOs used in this change (`UserResponse`, `NutritionalGoal`, `DailySummary`, `WeeklySummary`, `SummaryCard`, `MealLog`, `MealItem`, `Food`)
- [x] 1.3 Create `src/lib/api.ts` — fetch wrapper that reads `macromind_user_id` from localStorage, injects `X-User-Id` header, sets `Content-Type: application/json`, and throws on non-2xx

## 2. Shell — Routing, Layout, User Context

- [x] 2.1 Create `src/context/UserContext.tsx` — React context providing `userId: string | null` read from localStorage; `setUserId` persists to localStorage
- [x] 2.2 Create `src/components/Layout.tsx` — sidebar with MacroMind logo and nav links (Dashboard, Meal Log); renders `<Outlet />` for child pages
- [x] 2.3 Wire `App.tsx` with React Router: root layout at `/`, redirect to `/register` when no userId, `/dashboard` and `/meal-log` as child routes, replace placeholder content

## 3. Registration Page

- [x] 3.1 Create `src/pages/Register.tsx` — form with fields: name, email, age, weight (kg), height (cm), goal type (select: LOSE_WEIGHT | MAINTAIN_WEIGHT | GAIN_MUSCLE); calls `POST /api/v1/users`, stores returned `id` in localStorage via UserContext, navigates to `/dashboard`
- [x] 3.2 Add inline validation: all fields required, age 1–120, weight 20–500, height 50–300; disable submit while loading

## 4. Dashboard Page

- [x] 4.1 Create `src/pages/Dashboard.tsx` — fetches `/api/v1/dashboard/summary` on mount; renders summary card with four macro progress bars (calories, protein, carbs, fat) showing value/target and percentage; handles null targets (no goal state)
- [x] 4.2 Add weekly bar chart to Dashboard — fetches `/api/v1/dashboard/weekly?weekStart=<monday>` and renders a Recharts `BarChart` with 7 day bars showing daily calories
- [x] 4.3 Add inline goal form to Dashboard — fetches `GET /api/v1/nutritional-goals` on mount; renders form pre-filled with existing values (or empty if none); calls `PUT /api/v1/nutritional-goals` on submit and refreshes summary card

## 5. Meal Logging Page

- [x] 5.1 Create `src/pages/MealLog.tsx` — fetches `/api/v1/meal-logs?date=<today>` on mount; groups logs by meal type (BREAKFAST, LUNCH, DINNER, SNACK); each section shows logs or an empty state with "Add meal" button
- [x] 5.2 Add create/delete meal log — "Add meal" button calls `POST /api/v1/meal-logs` with the section's meal type; delete button (with confirmation) calls `DELETE /api/v1/meal-logs/{id}`; both re-fetch the day's logs on success
- [x] 5.3 Expand meal log to show items — each meal log is expandable; expanded view shows food items with name, quantity, and computed macros; totals row at the bottom
- [x] 5.4 Add food item form — inside expanded meal log: text input calls `GET /api/v1/foods?search=<term>` (debounced 300ms) and shows a dropdown; selecting a food + entering quantity calls `POST /api/v1/meal-logs/{id}/items`; remove button calls `DELETE /api/v1/meal-logs/{id}/items/{itemId}`
