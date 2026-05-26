## ADDED Requirements

### Requirement: Copy previous day's meals from the meal log page header
The system SHALL display a "Copy previous day" button in the meal log page header that duplicates the prior day's meal logs into the currently selected date.

#### Scenario: Button triggers copy and refreshes the page
- **WHEN** the user clicks "Copy previous day"
- **THEN** the app calls `POST /api/v1/meal-logs/copy-previous-day` with the selected date and, on success, reloads the meal logs for the selected date

#### Scenario: Button is disabled while the copy is in progress
- **WHEN** the user clicks "Copy previous day" and the request is pending
- **THEN** the button is disabled and shows a loading indicator until the request completes

#### Scenario: Feedback when the previous day has no meals
- **WHEN** the copy response returns an empty list
- **THEN** the page displays an inline message indicating there were no meals on the previous day to copy

#### Scenario: Button is not shown for dates in the past beyond yesterday
- **WHEN** the user is viewing a date that is not today
- **THEN** the "Copy previous day" button is still shown, using the day before the selected date as the source
