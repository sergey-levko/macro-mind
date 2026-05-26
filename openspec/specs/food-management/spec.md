### Requirement: User can view their food library
The system SHALL display foods belonging to the authenticated user in a paginated list showing name, calories/100g, protein, carbs, and fat. The list SHALL be sorted alphabetically by name. `GET /api/v1/foods` accepts optional `page` (default 0) and `size` (default 20, max 50) query parameters and returns a page envelope: `{ content, page, totalPages, totalElements }`.

#### Scenario: Foods tab loads the first page
- **WHEN** the user navigates to the Foods tab
- **THEN** the system displays the first page of the user's foods (up to 20 items), sorted by name ascending
- **THEN** pagination controls show the current page number and total page count

#### Scenario: User navigates to the next page
- **WHEN** the user clicks the "Next" button and a next page exists
- **THEN** the system fetches page+1 and updates the list
- **THEN** the "Next" button is disabled when the last page is reached

#### Scenario: User navigates to the previous page
- **WHEN** the user clicks the "Previous" button and the current page is greater than 0
- **THEN** the system fetches page-1 and updates the list
- **THEN** the "Previous" button is disabled on page 0

#### Scenario: User filters foods by name
- **WHEN** the user types in the search field
- **THEN** the list resets to page 0 and shows only foods whose name matches the query

#### Scenario: No foods exist
- **WHEN** the user has no foods in the database
- **THEN** the system displays an empty-state message and hides pagination controls

#### Scenario: Page size is capped server-side
- **WHEN** `GET /api/v1/foods` is called with `size` greater than 50
- **THEN** the system clamps the page size to 50

### Requirement: User can edit a food
The system SHALL allow the user to update the name, calories/100g, protein, carbs, and fat of any food they own via `PUT /api/v1/foods/{id}`.

#### Scenario: Successful edit
- **WHEN** the user submits a valid update form for a food they own
- **THEN** the system saves the updated values and reflects them in the list

#### Scenario: Edit another user's food is rejected
- **WHEN** the user attempts to update a food they do not own
- **THEN** the system returns 403 Forbidden

#### Scenario: Edit with invalid data is rejected
- **WHEN** the user submits a form with a missing name or negative numeric value
- **THEN** the system returns 400 Bad Request and displays a validation error

### Requirement: User can delete a food not referenced by meal logs
The system SHALL allow the user to delete a food they own that is not referenced by any meal item, returning 204 No Content on success.

#### Scenario: Successful delete
- **WHEN** the user confirms deletion of a food not used in any meal log
- **THEN** the system removes the food and it no longer appears in the list

#### Scenario: Delete another user's food is rejected
- **WHEN** the user attempts to delete a food they do not own
- **THEN** the system returns 403 Forbidden

### Requirement: Delete is rejected when food is in use
The system SHALL return 409 Conflict when the user attempts to delete a food that is referenced by one or more meal items.

#### Scenario: Food is referenced by a meal item
- **WHEN** the user attempts to delete a food that appears in at least one meal log
- **THEN** the system returns 409 Conflict with a message indicating the food is in use
- **THEN** the food is not deleted

#### Scenario: Error is surfaced to the user
- **WHEN** the backend returns 409 for a delete attempt
- **THEN** the frontend displays a user-readable error message explaining the food cannot be deleted
