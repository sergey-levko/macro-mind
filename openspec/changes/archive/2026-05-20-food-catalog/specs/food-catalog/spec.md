## ADDED Requirements

### Requirement: Create a custom food
The system SHALL allow a user to create a custom food entry with full macro data per 100g.

#### Scenario: Successful creation
- **WHEN** `POST /api/v1/foods` is called with a valid body containing `name`, `calories_100g`, `protein_g`, `carbs_g`, `fat_g`, and `X-User-Id` header with a valid user UUID
- **THEN** the system persists the food with `source = "CUSTOM"`, returns HTTP 201 with the created food's `id`, `name`, `source`, `calories_100g`, `protein_g`, `carbs_g`, `fat_g`

#### Scenario: Creation fails when required fields are missing
- **WHEN** `POST /api/v1/foods` is called with one or more required fields absent
- **THEN** the system returns HTTP 400 Bad Request with a validation error listing the missing fields

#### Scenario: Creation fails when user does not exist
- **WHEN** `POST /api/v1/foods` is called with an `X-User-Id` that does not match any user in the `users` table
- **THEN** the system returns HTTP 404 Not Found

### Requirement: Retrieve a food by ID
The system SHALL return the full details of a food entry identified by its UUID.

#### Scenario: Successful retrieval
- **WHEN** `GET /api/v1/foods/{id}` is called with a valid UUID that exists in the `foods` table
- **THEN** the system returns HTTP 200 with the food's `id`, `name`, `source`, `calories_100g`, `protein_g`, `carbs_g`, `fat_g`

#### Scenario: Food not found
- **WHEN** `GET /api/v1/foods/{id}` is called with a UUID that does not exist in the `foods` table
- **THEN** the system returns HTTP 404 Not Found

#### Scenario: Retrieval fails with invalid UUID format
- **WHEN** `GET /api/v1/foods/{id}` is called with a string that is not a valid UUID
- **THEN** the system returns HTTP 400 Bad Request

### Requirement: Search foods by name
The system SHALL return up to 20 foods whose name contains the search term (case-insensitive), scoped to the requesting user's own foods.

#### Scenario: Successful search with results
- **WHEN** `GET /api/v1/foods?search=chicken` is called with a valid `X-User-Id`
- **THEN** the system returns HTTP 200 with an array of up to 20 foods whose `name` contains "chicken" (case-insensitive) owned by that user

#### Scenario: Search returns empty list
- **WHEN** `GET /api/v1/foods?search=xyz123` is called and no matching foods exist for the user
- **THEN** the system returns HTTP 200 with an empty array

#### Scenario: Search without term returns all user foods
- **WHEN** `GET /api/v1/foods` is called without a `search` parameter and a valid `X-User-Id`
- **THEN** the system returns HTTP 200 with up to 20 foods owned by that user

### Requirement: Delete a user-owned food
The system SHALL allow a user to delete a custom food they own.

#### Scenario: Successful deletion
- **WHEN** `DELETE /api/v1/foods/{id}` is called with a valid UUID and the food belongs to the user identified by `X-User-Id`
- **THEN** the system removes the food from the `foods` table and returns HTTP 204 No Content

#### Scenario: Deletion fails when food not found
- **WHEN** `DELETE /api/v1/foods/{id}` is called with a UUID that does not exist
- **THEN** the system returns HTTP 404 Not Found

#### Scenario: Deletion fails when food belongs to another user
- **WHEN** `DELETE /api/v1/foods/{id}` is called and the food exists but belongs to a different user
- **THEN** the system returns HTTP 403 Forbidden

### Requirement: Import a food from USDA FoodData Central
The system SHALL fetch food data from the USDA FoodData Central API by `fdcId` and persist it locally in the `foods` table.

#### Scenario: Successful import
- **WHEN** `POST /api/v1/foods/import` is called with a valid `fdcId` and a valid `X-User-Id`
- **THEN** the system fetches the food from the USDA API, persists it with `source = "USDA"`, and returns HTTP 201 with the created food's full details

#### Scenario: Import fails when fdcId does not exist in USDA
- **WHEN** `POST /api/v1/foods/import` is called with an `fdcId` that USDA does not recognize
- **THEN** the system returns HTTP 404 Not Found with a message indicating the USDA food was not found

#### Scenario: Import fails when USDA API is unavailable
- **WHEN** `POST /api/v1/foods/import` is called and the USDA API cannot be reached
- **THEN** the system returns HTTP 503 Service Unavailable
