## ADDED Requirements

### Requirement: Copy all meal logs from the previous day to a target date
The system SHALL provide an endpoint that duplicates every meal log and its items from the calendar day before a given target date into that target date for the authenticated user.

#### Scenario: Copies all logs and items from the previous day
- **WHEN** the authenticated user calls `POST /api/v1/meal-logs/copy-previous-day` with body `{ "date": "2024-01-02" }`
- **THEN** the system creates new meal logs on `2024-01-02` (one per meal log from `2024-01-01`) each with `loggedAt` set to midnight UTC of `2024-01-02`, copies all items with their original `food_id` and `quantity_g`, and returns the list of newly created `MealLogSummaryResponse`

#### Scenario: Returns empty list when source day has no meals
- **WHEN** the authenticated user calls `POST /api/v1/meal-logs/copy-previous-day` with a date whose previous day has no meal logs
- **THEN** the system returns HTTP 200 with an empty JSON array `[]` and creates no new records

#### Scenario: Copy is additive when target date already has meals
- **WHEN** the authenticated user already has meal logs on the target date and calls `POST /api/v1/meal-logs/copy-previous-day` for that date
- **THEN** the system creates additional meal logs on the target date without removing or modifying existing ones

#### Scenario: Only copies the authenticated user's own meals
- **WHEN** user A and user B both have meals on the same source date and user A calls `POST /api/v1/meal-logs/copy-previous-day`
- **THEN** only user A's meal logs are copied; user B's data is unaffected

#### Scenario: Copy is atomic — all logs and items succeed or none are created
- **WHEN** an error occurs mid-copy (e.g. constraint violation on the second meal log)
- **THEN** no new records are persisted for that request
