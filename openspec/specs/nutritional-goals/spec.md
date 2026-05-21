## Requirements

### Requirement: Set nutritional goal
The system SHALL allow a user to create or replace their nutritional goal with daily macro targets.

#### Scenario: Successful creation when no goal exists
- **WHEN** `PUT /api/v1/nutritional-goals` is called with a valid `X-User-Id`, a positive `caloriesTarget`, and positive values for `proteinG`, `carbsG`, `fatG`
- **THEN** the system persists the goal and returns HTTP 200 with the goal's `id`, `userId`, `caloriesTarget`, `proteinG`, `carbsG`, and `fatG`

#### Scenario: Successful replacement when a goal already exists
- **WHEN** `PUT /api/v1/nutritional-goals` is called and the user already has an existing goal
- **THEN** the system replaces the previous goal and returns HTTP 200 with the updated values

#### Scenario: Creation fails when user does not exist
- **WHEN** `PUT /api/v1/nutritional-goals` is called with an `X-User-Id` that does not match any user
- **THEN** the system returns HTTP 404 Not Found

#### Scenario: Creation fails when a required field is missing or not positive
- **WHEN** `PUT /api/v1/nutritional-goals` is called without one of `caloriesTarget`, `proteinG`, `carbsG`, `fatG`, or with a value ≤ 0
- **THEN** the system returns HTTP 400 Bad Request

### Requirement: Retrieve nutritional goal
The system SHALL return the current nutritional goal for the requesting user.

#### Scenario: Successful retrieval
- **WHEN** `GET /api/v1/nutritional-goals` is called with a valid `X-User-Id` that has a goal
- **THEN** the system returns HTTP 200 with `id`, `userId`, `caloriesTarget`, `proteinG`, `carbsG`, `fatG`

#### Scenario: Retrieval fails when no goal is set
- **WHEN** `GET /api/v1/nutritional-goals` is called and the user has no nutritional goal
- **THEN** the system returns HTTP 404 Not Found

### Requirement: Delete nutritional goal
The system SHALL allow a user to remove their current nutritional goal.

#### Scenario: Successful deletion
- **WHEN** `DELETE /api/v1/nutritional-goals` is called with a valid `X-User-Id` that has a goal
- **THEN** the system removes the goal and returns HTTP 204 No Content

#### Scenario: Deletion fails when no goal is set
- **WHEN** `DELETE /api/v1/nutritional-goals` is called and the user has no nutritional goal
- **THEN** the system returns HTTP 404 Not Found

### Requirement: Generate AI-suggested nutritional goal
The system SHALL provide an endpoint that uses Claude to generate a nutritional goal suggestion for the user without persisting it.

#### Scenario: Successful generation
- **WHEN** `POST /api/v1/nutritional-goals/generate` is called with a valid `X-User-Id`
- **THEN** the system returns HTTP 200 with `{ caloriesTarget, proteinG, carbsG, fatG }` derived from the user's profile via Claude; no record is written to the database

#### Scenario: Generation fails when user does not exist
- **WHEN** `POST /api/v1/nutritional-goals/generate` is called with an `X-User-Id` that does not match any user
- **THEN** the system returns HTTP 404 Not Found

#### Scenario: Generation fails on AI parsing error
- **WHEN** Claude returns output that cannot be parsed into numeric macro values
- **THEN** the system returns HTTP 502 Bad Gateway
