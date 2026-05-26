## Requirements

### Requirement: Retrieve the user's most recently used foods
The system SHALL provide an endpoint that returns the foods most recently added as meal items by the authenticated user, ordered by last-used descending and deduplicated by food ID, up to a configurable limit (default 5).

#### Scenario: Successful retrieval with results
- **WHEN** `GET /api/v1/foods/recent` is called with a valid JWT and the user has prior meal items
- **THEN** the system returns HTTP 200 with an array of up to 5 distinct foods ordered by most recently used, each containing `id`, `name`, `calories_100g`, `protein_g`, `carbs_g`, `fat_g`

#### Scenario: User has no meal history
- **WHEN** `GET /api/v1/foods/recent` is called and the user has never added any meal items
- **THEN** the system returns HTTP 200 with an empty array

#### Scenario: Deduplication by food ID
- **WHEN** the same food has been added as a meal item multiple times across different logs
- **THEN** it appears only once in the response, representing the most recent use
