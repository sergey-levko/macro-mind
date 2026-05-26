## ADDED Requirements

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

### Requirement: Copy previous day's meals from the meal log page header
The system SHALL display a "Copy yesterday's meals" button in the meal log page header. When clicked, it SHALL call `POST /api/v1/meal-logs/copy` with `sourceDate` set to the day before the currently selected date and `targetDate` set to the currently selected date, then refresh the meal log view.

#### Scenario: User copies previous day's meals
- **WHEN** the user clicks "Copy yesterday's meals" in the page header
- **THEN** the app calls `POST /api/v1/meal-logs/copy` with `sourceDate` as the day before the selected date and `targetDate` as the selected date, and the meal log view refreshes to show the copied meals

#### Scenario: Button is hidden when previous day has no meals
- **WHEN** the previous day has no meal logs
- **THEN** the "Copy yesterday's meals" button is not rendered or is visually disabled

#### Scenario: Copy operation shows a loading state
- **WHEN** the copy request is in-flight
- **THEN** the button is disabled and shows a loading indicator to prevent duplicate submissions

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

#### Scenario: Recent foods are shown before the user starts typing
- **WHEN** the user opens the food search input and has not yet typed anything
- **THEN** the app calls `GET /api/v1/foods/recent` and displays up to 5 recently used foods as quick-add suggestions in the dropdown

#### Scenario: Quick-adding a recent food skips manual search
- **WHEN** the user selects a food from the recent foods suggestions and enters a quantity
- **THEN** the app calls `POST /api/v1/meal-logs/{id}/items` with the selected food's `foodId` and the entered `quantityG`, adding the item without requiring a separate search
