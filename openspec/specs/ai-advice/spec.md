# Specification: AI Advice

## Requirement: Generate nutrition advice
The system SHALL generate personalized nutrition advice by analyzing the user's meal logs for a given period against their nutritional goal and calling the Claude AI API.

### Scenario: Successful advice generation
- **WHEN** `POST /api/v1/advice` is called with a valid `X-User-Id`, a valid `adviceType` (DAILY/WEEKLY), and a valid `periodStart` (ISO-8601 date)
- **THEN** the system aggregates the user's meal logs for the period, calls the Claude AI API with the user's profile and nutritional goal as context, persists the advice, and returns HTTP 201 with `id`, `userId`, `adviceType`, `periodStart`, `content`, and `createdAt`

### Scenario: Generation fails when user does not exist
- **WHEN** `POST /api/v1/advice` is called with an `X-User-Id` that does not match any user
- **THEN** the system returns HTTP 404 Not Found

### Scenario: Generation fails when user has no nutritional goal
- **WHEN** `POST /api/v1/advice` is called and the user has no nutritional goal set
- **THEN** the system returns HTTP 400 Bad Request

### Scenario: Generation fails when adviceType is missing or invalid
- **WHEN** `POST /api/v1/advice` is called without `adviceType` or with an unrecognized value
- **THEN** the system returns HTTP 400 Bad Request

### Scenario: Generation fails when periodStart is missing or invalid
- **WHEN** `POST /api/v1/advice` is called without `periodStart` or with a non-date string
- **THEN** the system returns HTTP 400 Bad Request

## Requirement: Retrieve advice by ID
The system SHALL return a previously generated advice record by its identifier.

### Scenario: Successful retrieval
- **WHEN** `GET /api/v1/advice/{id}` is called with a valid UUID that exists
- **THEN** the system returns HTTP 200 with `id`, `userId`, `adviceType`, `periodStart`, `content`, and `createdAt`

### Scenario: Advice not found
- **WHEN** `GET /api/v1/advice/{id}` is called with a UUID that does not exist
- **THEN** the system returns HTTP 404 Not Found

## Requirement: List advice for a user
The system SHALL return all stored advice records for the requesting user, optionally filtered by type and period.

### Scenario: Successful listing with results
- **WHEN** `GET /api/v1/advice` is called with a valid `X-User-Id` and optional `adviceType` and/or `periodStart` query parameters
- **THEN** the system returns HTTP 200 with an array of matching advice records (each with `id`, `adviceType`, `periodStart`, `createdAt`, and `content`)

### Scenario: No advice records found
- **WHEN** `GET /api/v1/advice` is called and the user has no matching advice records
- **THEN** the system returns HTTP 200 with an empty array
