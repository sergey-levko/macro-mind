## ADDED Requirements

### Requirement: Retrieve recently used foods for the authenticated user
The system SHALL provide an endpoint that returns the N most recently used distinct foods for the authenticated user, ordered by the most recent usage date descending.

#### Scenario: Returns recent foods ordered by last-used date
- **WHEN** the authenticated user calls `GET /api/v1/foods/recent?limit=10`
- **THEN** the system returns up to 10 `FoodResponse` objects representing distinct foods, ordered by the most recent `meal_log.logged_at` descending

#### Scenario: Respects the authenticated user's scope
- **WHEN** user A and user B have both logged foods
- **THEN** `GET /api/v1/foods/recent` for user A returns only foods from user A's meal logs

#### Scenario: Returns empty list when user has no logged foods
- **WHEN** the authenticated user has never added a food item to any meal log
- **THEN** `GET /api/v1/foods/recent` returns an empty JSON array `[]`

#### Scenario: Deduplicates foods logged multiple times
- **WHEN** the user has logged the same food across multiple meal logs
- **THEN** that food appears exactly once in the recent list, positioned by its most recent usage

#### Scenario: Limit is capped at 10
- **WHEN** the user calls `GET /api/v1/foods/recent?limit=50`
- **THEN** the server returns at most 10 foods regardless of the requested limit
