## Why

Some users prefer to search only their own custom food library and do not want USDA results mixed into food search, whether for privacy, offline use, or simply to reduce noise. There is currently no way to opt out of the USDA integration short of ignoring its results — adding a per-user toggle gives users explicit control.

## What Changes

- Add a `usda_enabled` boolean column (default `true`) to the `users` table via Liquibase changeset
- Add REST endpoints `GET /api/v1/settings` and `PUT /api/v1/settings` to read and update the authenticated user's settings
- When `usda_enabled = false`: `GET /api/v1/foods/usda-search` returns an empty list and `POST /api/v1/foods/import` returns HTTP 403
- Add a Settings page at `/settings` with a sidebar nav link, containing a toggle for "Use USDA food database"
- The toggle state is fetched on page load and persisted immediately on change
- The food search in Meal Log hides the USDA section when the user's setting is off

## Capabilities

### New Capabilities

- `user-settings`: Per-user settings store — read and write user preferences via `GET/PUT /api/v1/settings`; initial preference: `usdaEnabled`
- `frontend-settings`: Settings page at `/settings` with a sidebar link; renders toggles for user preferences loaded from the settings API

### Modified Capabilities

- `food-catalog`: `GET /api/v1/foods/usda-search` and `POST /api/v1/foods/import` respect the `usda_enabled` flag — both return early (empty list / 403) when the calling user has USDA disabled
- `frontend-meal-logging`: Food search panel conditionally shows or hides the USDA results section based on the user's `usdaEnabled` setting fetched from the API

## Non-goals

- No other settings beyond `usdaEnabled` in this change
- No per-request override of the toggle (no query param to force-enable USDA)
- No admin-level global toggle — this is per-user only
- No migration of existing USDA-sourced foods (foods already imported remain in the catalog)

## Impact

- **Database**: `users` table gains `usda_enabled BOOLEAN NOT NULL DEFAULT TRUE` (Liquibase changeset required)
- **New API endpoints**: `GET /api/v1/settings` → `{ "usdaEnabled": true }`, `PUT /api/v1/settings` → body `{ "usdaEnabled": false }`, returns updated settings
- **Modified API endpoints**: `GET /api/v1/foods/usda-search` — returns `[]` when `usda_enabled = false`; `POST /api/v1/foods/import` — returns HTTP 403 when `usda_enabled = false`
- **New backend slice**: `com.epam.macromind.settings` — entity/column mapping via `users`, service, controller, DTOs
- **Frontend**: new `Settings.tsx` page, new sidebar nav entry, `MealLog.tsx` food search modified to consume settings context
- **Database tables affected**: `users`
