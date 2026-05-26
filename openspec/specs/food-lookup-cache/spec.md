## Requirements

### Requirement: Cache USDA food lookup results to avoid redundant external API calls
The system SHALL maintain a local cache of USDA FoodData Central lookup results keyed by `fdcId`. When a food import is requested for an `fdcId` that has been fetched before, the system SHALL return the cached data without calling the USDA API again.

#### Scenario: First import for a given fdcId calls the USDA API and stores the result
- **WHEN** `POST /api/v1/foods/import` is called with an `fdcId` that has not been fetched before
- **THEN** the system calls the USDA FoodData Central API, persists the food in the `foods` table with `source = "USDA"`, stores the raw lookup result in the cache keyed by `fdcId`, and returns HTTP 201 with the created food's details

#### Scenario: Subsequent import for the same fdcId uses the cache
- **WHEN** `POST /api/v1/foods/import` is called with an `fdcId` that is already in the cache
- **THEN** the system does NOT call the USDA API, uses the cached nutrient data to create the food record, and returns HTTP 201 with the created food's details

#### Scenario: Cache miss falls through to the USDA API
- **WHEN** the cache does not contain an entry for the requested `fdcId`
- **THEN** the system calls the USDA API, stores the result in the cache, and proceeds normally

#### Scenario: USDA API unavailability is not masked by an empty cache entry
- **WHEN** the USDA API returns an error and no cache entry exists for the `fdcId`
- **THEN** the system does NOT store any cache entry and returns HTTP 503 Service Unavailable to the caller

#### Scenario: Cache lookup does not affect the user-scoped foods table uniqueness
- **WHEN** two different users import the same `fdcId`
- **THEN** each user receives their own food record in the `foods` table (scoped by `user_id`), and both benefit from the cache avoiding duplicate USDA API calls
