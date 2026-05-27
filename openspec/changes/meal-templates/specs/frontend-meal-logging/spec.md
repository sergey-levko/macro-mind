## MODIFIED Requirements

### Requirement: Copy previous day's meals from the meal log page header
The system SHALL display per-meal-type "Copy yesterday's meal" buttons inside each meal section (not the page header). When clicked, a button SHALL call `POST /api/v1/meal-logs/copy-previous-day` with `date` set to the currently selected date and `mealType` set to the section's meal type, then refresh the meal log view. The buttons SHALL only be shown when the selected date is today.

#### Scenario: User copies previous day's meals for a single meal type
- **WHEN** the user clicks "Copy yesterday's meal" in a meal type section while viewing today
- **THEN** the app calls `POST /api/v1/meal-logs/copy-previous-day` with `date` as today and `mealType` as that section's type, and the section refreshes to show the copied meals

#### Scenario: Copy shows inline message when previous day has no meals for that type
- **WHEN** the copy response returns an empty array
- **THEN** the section shows a brief inline message "No meals yesterday" that auto-clears after 3 seconds

#### Scenario: Copy button is disabled while request is in-flight
- **WHEN** the copy request for a meal type is in-flight
- **THEN** that section's copy button is disabled to prevent duplicate submissions

#### Scenario: Copy buttons are hidden when not viewing today
- **WHEN** the user is viewing a past date
- **THEN** no "Copy yesterday's meal" buttons are visible in any meal section

## ADDED Requirements

### Requirement: Meal templates tab in Meal Log page
The system SHALL provide a Templates tab in the Meal Log page where the user can save the current date's meals as a named template, view all saved templates with macro previews, apply a template to the current date, and delete templates.

#### Scenario: Switching to the Templates tab shows saved templates
- **WHEN** the user clicks the "Templates" tab
- **THEN** the app calls `GET /api/v1/meal-templates` and displays each template with its name, item count, and macro totals (calories, protein, carbs, fat)

#### Scenario: Save as template prompts for a name then saves
- **WHEN** the user clicks "Save today as template", enters a name, and confirms
- **THEN** the app calls `POST /api/v1/meal-templates` with the entered name and the currently selected date, and the new template appears in the list

#### Scenario: Save is disabled when no meals are logged for the selected date
- **WHEN** the user is on the Templates tab and the selected date has no logged meals
- **THEN** the "Save today as template" button is disabled

#### Scenario: Applying a template to the current date
- **WHEN** the user clicks "Apply" on a template while viewing a date
- **THEN** the app calls `POST /api/v1/meal-templates/{id}/apply` with the selected date, and navigates to the Log tab and refreshes the meal log view to show the applied meals

#### Scenario: Deleting a template removes it from the list
- **WHEN** the user clicks "Delete" on a template and confirms
- **THEN** the app calls `DELETE /api/v1/meal-templates/{id}` and the template is removed from the list without a full page reload

#### Scenario: Empty state when no templates exist
- **WHEN** the user opens the Templates tab and has no saved templates
- **THEN** the tab shows a message prompting the user to save their first template
