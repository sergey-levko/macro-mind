## 1. Backend — Update User Endpoint

- [x] 1.1 Create `UpdateUserRequest` record with `name`, `age`, `weight_kg`, `height_cm`, `goal_type` fields and Bean Validation annotations matching `CreateUserRequest`
- [x] 1.2 Add `updateUser(UUID id, UpdateUserRequest request)` method to `UserService` — load entity, update fields, save, return `UserResponse`
- [x] 1.3 Add `PUT /api/v1/users/{id}` endpoint to `UserController` returning 200 with `UserResponse`
- [x] 1.4 Add unit tests to `UserServiceTest` for update: success, user not found (404), invalid input
- [x] 1.5 Add integration tests to `UserControllerIntegrationTest` for `PUT /api/v1/users/{id}`: success, 404, 400

## 2. Frontend — Profile Page

- [x] 2.1 Create `frontend/src/pages/Profile.tsx` — fetches `GET /api/v1/users/{userId}` on mount, renders read-only email field plus editable fields for `name`, `age`, `weight_kg`, `height_cm`, `goal_type` (select with three options)
- [x] 2.2 Implement save handler in `Profile.tsx` — calls `PUT /api/v1/users/{userId}`, shows success toast "Profile updated" or error toast "Failed to update profile, please try again"; Save button disabled while in flight

## 3. Frontend — Navigation

- [x] 3.1 Add `Profile` `NavLink` to `Layout.tsx` sidebar (same style as Dashboard / Meal Log links)
- [x] 3.2 Register `/profile` route in `App.tsx` pointing to the new `Profile` page
