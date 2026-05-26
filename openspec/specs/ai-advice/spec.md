# Specification: AI Advice

## Requirement: Generate nutrition advice
The system SHALL generate personalized nutrition advice by analyzing the user's meal logs for a given period against their nutritional goal and calling the Claude AI API. For non-preview requests, the system SHALL be idempotent: if advice for the same `(adviceType, periodStart)` already exists for the user, the existing record MUST be returned without calling the AI API again.

### Scenario: Successful advice generation (new record)
- **WHEN** `POST /api/v1/advice` is called with a valid JWT, a valid `adviceType` (DAILY/WEEKLY), a valid `periodStart` (ISO-8601 date), and no advice record exists yet for that `(adviceType, periodStart)` pair
- **THEN** the system aggregates the user's meal logs for the period, calls the Claude AI API with the user's profile and nutritional goal as context, persists the advice, and returns HTTP 202 Accepted with `id`, `userId`, `adviceType`, `periodStart`, `content`, and `createdAt`

### Scenario: Duplicate non-preview request returns existing record
- **WHEN** `POST /api/v1/advice` is called with a valid JWT, a valid `adviceType`, a valid `periodStart`, `preview` is absent or `false`, and an advice record already exists for that `(userId, adviceType, periodStart)` triple
- **THEN** the system returns HTTP 200 with the existing advice record without calling the AI API

### Scenario: Preview request always calls the AI
- **WHEN** `POST /api/v1/advice` is called with `preview: true` and an advice record already exists for the same `(userId, adviceType, periodStart)` triple
- **THEN** the system calls the Claude AI API and returns the freshly generated content without persisting it or modifying the existing record

### Scenario: Generation fails when user does not exist
- **WHEN** `POST /api/v1/advice` is called with a JWT whose user does not match any user record
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

## Requirement: Delete advice by ID
The system SHALL allow a user to permanently delete one of their own saved advice records.

### Scenario: Successful deletion
- **WHEN** `DELETE /api/v1/advice/{id}` is called with a valid JWT and `{id}` is a UUID that belongs to the requesting user
- **THEN** the system permanently removes the record from the database and returns HTTP 204 No Content

### Scenario: Deletion fails when advice not found
- **WHEN** `DELETE /api/v1/advice/{id}` is called with a UUID that does not exist in the database
- **THEN** the system returns HTTP 404 Not Found

### Scenario: Deletion fails when advice belongs to another user
- **WHEN** `DELETE /api/v1/advice/{id}` is called with a UUID that exists but belongs to a different user
- **THEN** the system returns HTTP 404 Not Found (ownership not disclosed)
