## MODIFIED Requirements

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
