## ADDED Requirements

### Requirement: Update user profile
The system SHALL allow a user to update their profile fields via a PUT request.

#### Scenario: Successful profile update
- **WHEN** `PUT /api/v1/users/{id}` is called with a valid UUID and a JSON body containing `name`, `age`, `weight_kg`, `height_cm`, and `goal_type`
- **THEN** the system persists the updated values and returns HTTP 200 with the full profile: `id`, `name`, `email`, `age`, `weight_kg`, `height_cm`, and `goal_type`

#### Scenario: Update fails when user does not exist
- **WHEN** `PUT /api/v1/users/{id}` is called with a UUID that does not exist in the `users` table
- **THEN** the system returns HTTP 404 Not Found

#### Scenario: Update fails when required fields are missing or invalid
- **WHEN** `PUT /api/v1/users/{id}` is called with one or more required fields absent, blank, or out of valid range
- **THEN** the system returns HTTP 400 Bad Request with a validation error

#### Scenario: Update fails when goal_type is invalid
- **WHEN** `PUT /api/v1/users/{id}` is called with a `goal_type` value not in `[LOSE_WEIGHT, MAINTAIN_WEIGHT, GAIN_MUSCLE]`
- **THEN** the system returns HTTP 400 Bad Request
