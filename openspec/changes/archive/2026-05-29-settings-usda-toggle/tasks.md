## 1. Database — Schema migration

- [x] 1.1 Add Liquibase changeset to `backend/src/main/resources/db/changelog/` that adds `usda_enabled BOOLEAN NOT NULL DEFAULT TRUE` column to the `users` table. Commit: `chore: add usda_enabled column to users table`

## 2. Backend — Settings slice

- [x] 2.1 Add `usda_enabled` field to the `User` entity and confirm JPA mapping picks it up from the Liquibase changeset column. Commit: `feat: map usda_enabled field on User entity`
- [x] 2.2 Create the `settings` vertical slice: `UserSettingsController`, `UserSettingsService`, `UserSettingsResponse` DTO, `UpdateUserSettingsRequest` DTO. Implement `GET /api/v1/settings` (reads `user.isUsdaEnabled()`) and `PUT /api/v1/settings` (updates `usda_enabled` via `UserRepository.save`). Commit: `feat: add GET/PUT /api/v1/settings endpoints`
- [x] 2.3 Write unit tests (`UserSettingsServiceTest`) covering: read returns current value, update persists new value. Write integration test (`UserSettingsIntegrationTest`) covering: default is `true`, toggle off then on via PUT, GET reflects updated value. Commit: `test: add user settings unit and integration tests`

## 3. Backend — Gate USDA endpoints on the setting

- [x] 3.1 In `FoodService` (or `FoodController`), before executing the USDA search logic in `GET /api/v1/foods/usda-search`, fetch the calling user's `usda_enabled` flag; return an empty list immediately when `false`. Commit: `feat: skip USDA search when usda_enabled is false`
- [x] 3.2 In `FoodService`, before executing USDA import logic in `POST /api/v1/foods/import`, check `usda_enabled`; throw a `UsdaDisabledException` (mapped to HTTP 403) when `false`. Add the exception class and a handler in `GlobalExceptionHandler`. Commit: `feat: block USDA import when usda_enabled is false`
- [x] 3.3 Add integration tests: USDA search returns empty when setting is off; USDA import returns 403 when setting is off; both work normally when setting is on. Commit: `test: add integration tests for USDA gating by user setting`

## 4. Frontend — Settings page and sidebar link

- [x] 4.1 Create `frontend/src/pages/Settings.tsx`: fetch `GET /api/v1/settings` on mount; render a toggle for "Use USDA food database"; on toggle change call `PUT /api/v1/settings` immediately; show loading state during the request; revert and show error toast on failure. Add the route `/settings` in the router. Commit: `feat: add Settings page with USDA toggle`
- [x] 4.2 Add a "Settings" link to the sidebar navigation component (`App.tsx` or the shell layout), pointing to `/settings`, styled consistently with existing nav links. Commit: `feat: add Settings link to sidebar navigation`

## 5. Frontend — Conditional USDA results in Meal Log

- [x] 5.1 In `MealLog.tsx` (`FoodItemForm`), fetch `GET /api/v1/settings` once on component mount (alongside the existing `GET /api/v1/foods/recent` call); store the `usdaEnabled` value in local state. Skip the `GET /api/v1/foods/usda-search` call and hide the USDA section in the dropdown when `usdaEnabled` is `false`. Commit: `feat: hide USDA food search results when setting is disabled`
