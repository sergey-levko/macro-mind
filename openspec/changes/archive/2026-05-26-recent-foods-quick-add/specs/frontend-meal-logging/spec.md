## MODIFIED Requirements

### Requirement: Add and remove food items from a meal log
The system SHALL allow the user to search for foods, select one, enter a quantity, and add it to a meal log as an item. When the search input is empty, the system SHALL display the user's most recently used foods as one-tap quick-add options above the search input.

#### Scenario: Recent foods shown when search is empty
- **WHEN** the food-add panel is opened and the search input is empty
- **THEN** the app calls `GET /api/v1/foods/recent?limit=10` and displays up to 10 recent foods in a "Recent" section above the search input

#### Scenario: Recent section hidden while searching
- **WHEN** the user types one or more characters in the search input
- **THEN** the "Recent" section is hidden and only the search results are shown

#### Scenario: Food search queries the API as user types
- **WHEN** the user types in the food search input (minimum 2 characters)
- **THEN** the app calls `GET /api/v1/foods?search=<term>` (debounced 300ms) and displays matching foods in a dropdown

#### Scenario: Selecting a food from recent and entering quantity adds it
- **WHEN** the user clicks a food in the "Recent" section, enters a quantity in grams, and submits
- **THEN** the app calls `POST /api/v1/meal-logs/{id}/items` with `foodId` and `quantityG`, and the item appears in the meal log with its computed macros

#### Scenario: Selecting a food and quantity adds it as an item
- **WHEN** the user selects a food from the search dropdown, enters a quantity in grams, and submits
- **THEN** the app calls `POST /api/v1/meal-logs/{id}/items` with `foodId` and `quantityG`, and the item appears in the meal log with its computed macros

#### Scenario: Removing a food item deletes it via API
- **WHEN** the user clicks the remove button on a food item
- **THEN** the app calls `DELETE /api/v1/meal-logs/{id}/items/{itemId}` and the item is removed from the list

#### Scenario: Recent section not shown when user has no history
- **WHEN** the food-add panel is opened, the search input is empty, and `GET /api/v1/foods/recent` returns an empty list
- **THEN** the "Recent" section is not rendered and only the search input is shown
