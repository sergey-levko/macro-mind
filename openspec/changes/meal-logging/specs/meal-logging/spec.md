## ADDED Requirements

### Requirement: Create a meal log
The system SHALL allow a user to create a meal log entry with a meal type and timestamp.

#### Scenario: Successful creation
- **WHEN** `POST /api/v1/meal-logs` is called with a valid `X-User-Id`, a valid `mealType` (BREAKFAST/LUNCH/DINNER/SNACK), and an optional `loggedAt` ISO-8601 datetime (defaults to now if omitted)
- **THEN** the system persists the meal log and returns HTTP 201 with the created log's `id`, `userId`, `mealType`, `loggedAt`, and an empty `items` array

#### Scenario: Creation fails when user does not exist
- **WHEN** `POST /api/v1/meal-logs` is called with an `X-User-Id` that does not match any user
- **THEN** the system returns HTTP 404 Not Found

#### Scenario: Creation fails when mealType is missing or invalid
- **WHEN** `POST /api/v1/meal-logs` is called without a `mealType` or with an unrecognized value
- **THEN** the system returns HTTP 400 Bad Request

### Requirement: Retrieve a meal log by ID
The system SHALL return the full details of a meal log including all items and computed macro totals.

#### Scenario: Successful retrieval
- **WHEN** `GET /api/v1/meal-logs/{id}` is called with a valid UUID that exists
- **THEN** the system returns HTTP 200 with `id`, `userId`, `mealType`, `loggedAt`, a list of `items` (each with `itemId`, `foodId`, `foodName`, `quantityG`, `calories`, `proteinG`, `carbsG`, `fatG`), and aggregate `totals` (`calories`, `proteinG`, `carbsG`, `fatG`) computed from all items

#### Scenario: Meal log not found
- **WHEN** `GET /api/v1/meal-logs/{id}` is called with a UUID that does not exist
- **THEN** the system returns HTTP 404 Not Found

#### Scenario: Retrieval fails with invalid UUID
- **WHEN** `GET /api/v1/meal-logs/{id}` is called with a string that is not a valid UUID
- **THEN** the system returns HTTP 400 Bad Request

### Requirement: List meal logs by date
The system SHALL return all meal logs for a user on a given calendar date.

#### Scenario: Successful listing with results
- **WHEN** `GET /api/v1/meal-logs?date=YYYY-MM-DD` is called with a valid `X-User-Id` and a valid date
- **THEN** the system returns HTTP 200 with an array of meal log summaries (each with `id`, `mealType`, `loggedAt`, and macro `totals`) for all logs whose `loggedAt` falls within that UTC calendar day

#### Scenario: No logs on the given date
- **WHEN** `GET /api/v1/meal-logs?date=YYYY-MM-DD` is called and the user has no logs on that date
- **THEN** the system returns HTTP 200 with an empty array

#### Scenario: Listing fails when date parameter is missing
- **WHEN** `GET /api/v1/meal-logs` is called without a `date` query parameter
- **THEN** the system returns HTTP 400 Bad Request

### Requirement: Delete a meal log
The system SHALL allow a user to delete a meal log they own, removing all its items.

#### Scenario: Successful deletion
- **WHEN** `DELETE /api/v1/meal-logs/{id}` is called and the log belongs to the user identified by `X-User-Id`
- **THEN** the system removes the meal log and all its items, returning HTTP 204 No Content

#### Scenario: Deletion fails when log not found
- **WHEN** `DELETE /api/v1/meal-logs/{id}` is called with a UUID that does not exist
- **THEN** the system returns HTTP 404 Not Found

#### Scenario: Deletion fails when log belongs to another user
- **WHEN** `DELETE /api/v1/meal-logs/{id}` is called and the log exists but belongs to a different user
- **THEN** the system returns HTTP 403 Forbidden

### Requirement: Add a food item to a meal log
The system SHALL allow a user to add a food entry with a quantity to an existing meal log.

#### Scenario: Successful item addition
- **WHEN** `POST /api/v1/meal-logs/{id}/items` is called with a valid `foodId` (UUID) and `quantityG` (positive decimal), and the log belongs to the user identified by `X-User-Id`
- **THEN** the system persists the meal item and returns HTTP 201 with the item's `itemId`, `foodId`, `foodName`, `quantityG`, and computed `calories`, `proteinG`, `carbsG`, `fatG`

#### Scenario: Item addition fails when log not found
- **WHEN** `POST /api/v1/meal-logs/{id}/items` is called with a log UUID that does not exist
- **THEN** the system returns HTTP 404 Not Found

#### Scenario: Item addition fails when food not found
- **WHEN** `POST /api/v1/meal-logs/{id}/items` is called with a `foodId` that does not exist in the `foods` table
- **THEN** the system returns HTTP 404 Not Found

#### Scenario: Item addition fails when log belongs to another user
- **WHEN** `POST /api/v1/meal-logs/{id}/items` is called and the log exists but belongs to a different user
- **THEN** the system returns HTTP 403 Forbidden

#### Scenario: Item addition fails when quantityG is missing or not positive
- **WHEN** `POST /api/v1/meal-logs/{id}/items` is called without `quantityG` or with a value ≤ 0
- **THEN** the system returns HTTP 400 Bad Request

### Requirement: Remove a food item from a meal log
The system SHALL allow a user to remove a specific item from a meal log they own.

#### Scenario: Successful item removal
- **WHEN** `DELETE /api/v1/meal-logs/{id}/items/{itemId}` is called and both the log and item exist and the log belongs to the user identified by `X-User-Id`
- **THEN** the system removes the item and returns HTTP 204 No Content

#### Scenario: Removal fails when item not found
- **WHEN** `DELETE /api/v1/meal-logs/{id}/items/{itemId}` is called with an `itemId` that does not exist within the log
- **THEN** the system returns HTTP 404 Not Found

#### Scenario: Removal fails when log belongs to another user
- **WHEN** `DELETE /api/v1/meal-logs/{id}/items/{itemId}` is called and the log belongs to a different user
- **THEN** the system returns HTTP 403 Forbidden
