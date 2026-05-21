## MODIFIED Requirements

### Requirement: View selected date's meals grouped by meal type
The system SHALL display the selected date's meal logs grouped by meal type (Breakfast, Lunch, Dinner, Snack).

#### Scenario: Meals load on page mount
- **WHEN** the user navigates to `/meal-log`
- **THEN** the page calls `GET /api/v1/meal-logs?date=<selected-date>` and renders each meal log under its corresponding meal type section

#### Scenario: Empty meal type section shows add prompt
- **WHEN** a meal type has no logs for the selected date
- **THEN** the section renders an empty state with an "Add meal" button

#### Scenario: Meal log shows macro totals
- **WHEN** a meal log is displayed
- **THEN** it shows the meal's total calories, protein, carbs, and fat aggregated from its items

### Requirement: Create and delete meal logs
The system SHALL allow the user to create a new meal log for a given meal type and delete existing ones.

#### Scenario: Adding a meal log creates it via API
- **WHEN** the user clicks "Add meal" in a meal type section
- **THEN** the app calls `POST /api/v1/meal-logs` with `mealType` set to that section's type and `loggedAt` set to midnight UTC of the selected date, and the new log appears in the section

#### Scenario: Deleting a meal log removes it from the view
- **WHEN** the user clicks the delete button on a meal log and confirms
- **THEN** the app calls `DELETE /api/v1/meal-logs/{id}` and the log disappears from the page without a full reload

### Requirement: Add and remove food items from a meal log
The system SHALL allow the user to search for foods, select one, enter a quantity, and add it to a meal log as an item.

#### Scenario: Food search queries the API as user types
- **WHEN** the user types in the food search input (minimum 2 characters)
- **THEN** the app calls `GET /api/v1/foods?search=<term>` (debounced 300ms) and displays matching foods in a dropdown

#### Scenario: Selecting a food and quantity adds it as an item
- **WHEN** the user selects a food from the dropdown, enters a quantity in grams, and submits
- **THEN** the app calls `POST /api/v1/meal-logs/{id}/items` with `foodId` and `quantityG`, and the item appears in the meal log with its computed macros

#### Scenario: Removing a food item deletes it via API
- **WHEN** the user clicks the remove button on a food item
- **THEN** the app calls `DELETE /api/v1/meal-logs/{id}/items/{itemId}` and the item is removed from the list
