## ADDED Requirements

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
