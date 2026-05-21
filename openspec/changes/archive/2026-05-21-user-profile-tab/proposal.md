## Why

Users currently have no way to view or update their profile after registration — name, weight, height, age, and goal type are locked in forever. This blocks meaningful AI suggestions when a user's situation changes (e.g. they've lost weight and need updated macro targets).

## What Changes

- Add `PUT /api/v1/users/{id}` backend endpoint to update user profile fields
- Add a **Profile** tab to the sidebar navigation
- Add a `/profile` frontend page that displays the current user's profile and allows editing all fields: `name`, `email`, `age`, `weight_kg`, `height_cm`, `goal_type`
- Show a success/error toast on save

## Capabilities

### New Capabilities

- `frontend-profile`: Profile page with view and edit form for all user fields

### Modified Capabilities

- `user-profile`: Add `PUT /api/v1/users/{id}` update endpoint with validation; add navigation link to profile in the shell

## Impact

- **Database**: `users` table — no schema changes; only update DML
- **API endpoints added**: `PUT /api/v1/users/{id}`
- **Frontend pages added**: `/profile`
- **Frontend modified**: `Layout.tsx` (add Profile nav link), `App.tsx` (add `/profile` route)
- **Backend files modified**: `UserController`, `UserService`, new `UpdateUserRequest` DTO

## Non-goals

- Password / authentication management
- Avatar / photo upload
- Account deletion
- Email uniqueness re-validation on update (out of scope for this iteration)
