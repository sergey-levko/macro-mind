## MODIFIED Requirements

### Requirement: Import a food from USDA FoodData Central
The system SHALL fetch food data from the USDA FoodData Central API by `fdcId` and persist it locally in the `foods` table. If the authenticated user has `usda_enabled = false`, the import SHALL be rejected.

#### Scenario: Successful import
- **WHEN** `POST /api/v1/foods/import` is called with a valid `fdcId`, a valid JWT, and the user has `usda_enabled = true`
- **THEN** the system fetches the food from the USDA API, persists it with `source = "USDA"`, and returns HTTP 201 with the created food's full details

#### Scenario: Import blocked when USDA is disabled
- **WHEN** `POST /api/v1/foods/import` is called and the authenticated user has `usda_enabled = false`
- **THEN** the system returns HTTP 403 Forbidden with a message indicating USDA integration is disabled

#### Scenario: Import fails when fdcId does not exist in USDA
- **WHEN** `POST /api/v1/foods/import` is called with an `fdcId` that USDA does not recognize
- **THEN** the system returns HTTP 404 Not Found with a message indicating the USDA food was not found

#### Scenario: Import fails when USDA API is unavailable
- **WHEN** `POST /api/v1/foods/import` is called and the USDA API cannot be reached
- **THEN** the system returns HTTP 503 Service Unavailable
