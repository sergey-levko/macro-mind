## Context

MacroMind currently calls the USDA FoodData Central API unconditionally for every food search in the meal log panel (`GET /api/v1/foods/usda-search`) and allows importing any food by `fdcId` (`POST /api/v1/foods/import`). There is no per-user preference layer. This change adds a lightweight settings system, starting with a single boolean preference (`usda_enabled`), and surfaces it through a new Settings page.

## Goals / Non-Goals

**Goals:**
- Store `usda_enabled` per user in the database
- Expose `GET /PUT /api/v1/settings` to read and update the setting
- Gate `GET /api/v1/foods/usda-search` and `POST /api/v1/foods/import` on the flag
- Add a Settings page with a toggle that persists the preference immediately
- Conditionally render the USDA section in the Meal Log food search

**Non-Goals:**
- Settings table for future settings — start with a column on `users`, migrate to a table if preferences grow
- Any other settings beyond `usdaEnabled`
- Caching the settings value on the frontend beyond the page session

## Decisions

**Store `usda_enabled` as a column on `users`, not a separate settings table.**
One boolean preference does not warrant a new table. A `users.usda_enabled` column is cheaper to query (no join), trivial to migrate, and the pattern is clear for a single value. If more settings are added later, extracting to a `user_settings` table is a straightforward migration.

**Settings backed by a new `settings` vertical slice (not merged into `user` slice).**
Even though the data lives on `users`, the settings concern is distinct from profile management. A dedicated `SettingsController` + `SettingsService` reading from `UserRepository` keeps the slices cohesive and avoids overloading the `user` slice. The service reads and updates only the `usda_enabled` column.

**Gate USDA endpoints in the `food` slice service layer, not a Spring Security filter.**
The check (`user.isUsdaEnabled()`) is domain logic, not an authentication rule. Putting it in `FoodService` methods keeps the enforcement close to the behavior and avoids a proliferation of security filters for business rules.

**Frontend fetches settings once on app load via a React context / hook.**
The settings value is small and stable. Fetching it once at startup and storing it in a context (or passed as a prop to `MealLog`) avoids redundant API calls. The Settings page updates the context value on save, so the Meal Log reflects the change without a page reload.

**Toggle saves immediately on change (no explicit Save button).**
Single-toggle settings pages with a Save button create unnecessary friction. An instant `PUT /api/v1/settings` on toggle flip is consistent with patterns users expect from modern settings UIs. The toggle shows a brief loading state during the request.

## Risks / Trade-offs

- **Risk**: `usda_enabled` defaults to `true` — existing users are unaffected by the migration.
  → No mitigation needed; this is the desired behavior.

- **Risk**: If the `PUT /api/v1/settings` request fails, the UI toggle snaps back.
  → Mitigation: optimistic UI update on toggle, revert on error with a toast notification.

- **Risk**: USDA search results cached in Caffeine may be served briefly after a user disables USDA.
  → Acceptable: the cache is for USDA API responses, not for `usda-search` endpoint results. The gate is checked before any external call, so no external call is made when disabled. Cached imports are unaffected (they've already been saved to the `foods` table).

## Migration Plan

1. Add Liquibase changeset: `ALTER TABLE users ADD COLUMN usda_enabled BOOLEAN NOT NULL DEFAULT TRUE`
2. Implement `settings` backend slice
3. Update `FoodService` to check `usda_enabled` for USDA operations
4. Add `Settings.tsx` page and sidebar link
5. Update `MealLog.tsx` to consume settings
6. No rollback concern — column has a default and does not affect existing rows
