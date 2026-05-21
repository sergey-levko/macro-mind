## Context

The app stores user profile fields (name, email, age, weight_kg, height_cm, goal_type) but exposes only `POST /api/v1/users` (register) and `GET /api/v1/users/{id}` (read). There is no update path. The frontend has no profile page; the sidebar has Dashboard and Meal Log only.

`X-User-Id` is the user-identity mechanism — the frontend stores a UUID in `localStorage` and sends it as a header on every API call.

## Goals / Non-Goals

**Goals:**
- Add `PUT /api/v1/users/{id}` with full-profile update and Bean Validation
- Add a `/profile` React page with view + edit form
- Add Profile nav link to the sidebar

**Non-Goals:**
- Email uniqueness re-validation on update
- Password / authentication
- Avatar upload

## Decisions

**Single PUT replaces entire profile** — A full-replace PUT keeps the DTO simple. Partial PATCH adds complexity (null vs absent distinction) with no benefit at this scale.

**Reuse `UserResponse` as the PUT response** — The existing record type already has all fields. No new DTO needed for the response.

**New `UpdateUserRequest` DTO** — Mirrors `CreateUserRequest` minus email (email is not editable; changing it raises uniqueness concerns deferred to non-goals). Fields: `name`, `age`, `weight_kg`, `height_cm`, `goal_type`. All required with the same Bean Validation constraints as registration.

**Frontend form mirrors Registration page style** — Same field set, same select for goal_type (LOSE_WEIGHT / MAINTAIN_WEIGHT / GAIN_MUSCLE). Keeps visual consistency without a new design pattern.

**Toast on save** — Consistent with the existing pattern established by GoalForm (Toast.tsx / `useToast`).

## Risks / Trade-offs

[Email locked out of update] → Email change is a non-goal; the field will be read-only on the profile page. Future change can address this with a dedicated email-change flow.

[No optimistic update] → Form re-fetches from server after successful PUT to ensure displayed data matches the database. Slightly slower but simpler.

## Migration Plan

No schema changes. `UserService.updateUser` runs a standard `save()` on the already-loaded entity. No Liquibase changeset needed.
