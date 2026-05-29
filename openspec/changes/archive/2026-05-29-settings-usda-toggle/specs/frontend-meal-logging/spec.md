## MODIFIED Requirements

### Requirement: Add and remove food items from a meal log
The system SHALL allow the user to search for foods, select one, enter a quantity, and add it to a meal log as an item. USDA search results SHALL only be shown when the user's `usdaEnabled` setting is `true`.

#### Scenario: Food search queries the API as user types
- **WHEN** the user types in the food search input (minimum 2 characters)
- **THEN** the app calls `GET /api/v1/foods?search=<term>` (debounced 300ms) and displays matching foods in a dropdown

#### Scenario: USDA results shown when setting is enabled
- **WHEN** the user types in the food search input and `usdaEnabled` is `true`
- **THEN** the app also calls `GET /api/v1/foods/usda-search?q=<term>` and displays USDA results in the dropdown below the local results

#### Scenario: USDA results hidden when setting is disabled
- **WHEN** the user types in the food search input and `usdaEnabled` is `false`
- **THEN** the app does NOT call `GET /api/v1/foods/usda-search` and no USDA section appears in the dropdown

#### Scenario: Selecting a food and quantity adds it as an item
- **WHEN** the user selects a food from the dropdown, enters a quantity in grams, and submits
- **THEN** the app calls `POST /api/v1/meal-logs/{id}/items` with `foodId` and `quantityG`, and the item appears in the meal log with its computed macros

#### Scenario: Removing a food item deletes it via API
- **WHEN** the user clicks the remove button on a food item
- **THEN** the app calls `DELETE /api/v1/meal-logs/{id}/items/{itemId}` and the item is removed from the list

#### Scenario: Recent foods are shown before the user starts typing
- **WHEN** the user opens the food search input and has not yet typed anything
- **THEN** the app calls `GET /api/v1/foods/recent` and displays up to 5 recently used foods as quick-add suggestions in the dropdown

#### Scenario: Quick-adding a recent food skips manual search
- **WHEN** the user selects a food from the recent foods suggestions and enters a quantity
- **THEN** the app calls `POST /api/v1/meal-logs/{id}/items` with the selected food's `foodId` and the entered `quantityG`, adding the item without requiring a separate search
