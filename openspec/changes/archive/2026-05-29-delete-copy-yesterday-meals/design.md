## Context

The `copy-previous-day-meals` capability was introduced before meal templates existed. It provided a one-click way to duplicate the previous day's meal logs onto the current date. Now that named meal templates cover the same use-case more flexibly (any date, any reusable set), the feature is redundant. This change is a clean removal across three layers: controller, service, and frontend component.

No database schema changes are needed — the feature operated purely as insert logic over existing `meal_logs` and `meal_items` tables.

## Goals / Non-Goals

**Goals:**
- Remove the `POST /api/v1/meal-logs/copy-previous-day` endpoint and all code that implements it
- Remove the "Copy previous day" UI button and its state logic from `MealLog.tsx`
- Delete the tests that cover the removed code
- Leave no dead code, unused imports, or orphaned DTOs behind

**Non-Goals:**
- No changes to meal templates or any other feature
- No data migration — existing meal logs created via this feature remain intact
- No deprecation endpoint returning 410 Gone (internal app, no external consumers)

## Decisions

**Remove the endpoint directly, no deprecation period.**
MacroMind is a single-team internal application with no published public API contract. A staged deprecation period would add complexity for no benefit. The frontend and backend are released together, so the UI and API can be removed atomically.

**Remove the DTO if it is used only by this endpoint.**
Rather than leaving a `CopyPreviousDayRequest` DTO on the classpath as dead code, it should be deleted. If the DTO is somehow shared, the relevant field should be extracted instead — but inspection will likely show it is endpoint-specific.

**Delete tests, do not disable them.**
Disabling or skipping tests for removed functionality leaves misleading test noise. All unit and integration tests covering `copyPreviousDay` should be deleted outright.

## Risks / Trade-offs

- **Risk**: A user relying solely on copy-yesterday loses quick access on the day templates were not set up yet.
  → **Mitigation**: Meal templates already exist and are accessible from the same Meal Log page. The Templates tab is the documented replacement path.

- **Risk**: Removing the button changes muscle memory for existing users.
  → **Mitigation**: Acceptable — this is an internal initiative demo app, not a production user-facing product with SLAs.

## Migration Plan

1. Remove backend: controller action → service method → DTO (if exclusive)
2. Remove backend tests covering the deleted code
3. Remove frontend: API call → button element → loading/disabled state
4. Commit atomically, verify no dead imports remain
5. No rollback plan needed — changes are in version control
