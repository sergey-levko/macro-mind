## Requirements

### Requirement: Navigate meal logs by date
The system SHALL allow the user to navigate between dates on the Meal Log page to view and manage meal logs for any past day up to and including today.

#### Scenario: Page defaults to today
- **WHEN** the user navigates to `/meal-log`
- **THEN** the selected date is today's date and meals for today are loaded via `GET /api/v1/meal-logs?date=<today>`

#### Scenario: Navigate to previous day
- **WHEN** the user clicks the "previous day" button
- **THEN** the selected date moves back one day, the date label updates, and meals for the new date are loaded via `GET /api/v1/meal-logs?date=<ISO>`

#### Scenario: Navigate to next day
- **WHEN** the user clicks the "next day" button and the selected date is not today
- **THEN** the selected date moves forward one day, the date label updates, and meals for the new date are loaded

#### Scenario: Next day button disabled on today
- **WHEN** the selected date equals today's date
- **THEN** the "next day" button is disabled, preventing navigation to future dates

#### Scenario: Meals added on a past date use that date's loggedAt
- **WHEN** the user adds a meal log while a past date is selected
- **THEN** the app calls `POST /api/v1/meal-logs` with `loggedAt` set to midnight UTC of the selected date
