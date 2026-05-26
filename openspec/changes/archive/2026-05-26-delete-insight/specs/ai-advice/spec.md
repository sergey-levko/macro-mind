## ADDED Requirements

### Requirement: Delete advice by ID
The system SHALL allow a user to permanently delete one of their own saved advice records.

#### Scenario: Successful deletion
- **WHEN** `DELETE /api/v1/advice/{id}` is called with a valid JWT and `{id}` is a UUID that belongs to the requesting user
- **THEN** the system permanently removes the record from the database and returns HTTP 204 No Content

#### Scenario: Deletion fails when advice not found
- **WHEN** `DELETE /api/v1/advice/{id}` is called with a UUID that does not exist in the database
- **THEN** the system returns HTTP 404 Not Found

#### Scenario: Deletion fails when advice belongs to another user
- **WHEN** `DELETE /api/v1/advice/{id}` is called with a UUID that exists but belongs to a different user
- **THEN** the system returns HTTP 404 Not Found (ownership not disclosed)
