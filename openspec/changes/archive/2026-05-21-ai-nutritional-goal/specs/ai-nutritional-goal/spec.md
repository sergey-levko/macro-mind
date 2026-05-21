## Requirements

### Requirement: Generate AI-powered nutritional goal suggestion
The system SHALL allow a user to request a Claude-generated nutritional goal suggestion based on their profile, without persisting it automatically.

#### Scenario: Successful generation returns macro targets
- **WHEN** `POST /api/v1/nutritional-goals/generate` is called with a valid `X-User-Id`
- **THEN** the system calls Claude with the user's `goal_type`, `age`, `weight_kg`, and `height_cm`, and returns HTTP 200 with `{ caloriesTarget, proteinG, carbsG, fatG }` without writing any record to the database

#### Scenario: Generation fails when user does not exist
- **WHEN** `POST /api/v1/nutritional-goals/generate` is called with an `X-User-Id` that does not match any user
- **THEN** the system returns HTTP 404 Not Found

#### Scenario: Generation fails when Claude returns unparseable output
- **WHEN** the Claude API returns a response that cannot be parsed into the expected numeric fields
- **THEN** the system returns HTTP 502 Bad Gateway with a descriptive error message
