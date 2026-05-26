## MODIFIED Requirements

### Requirement: Coach page displays proactive daily and weekly insights
The Coach page SHALL display an insights panel with two sub-tabs: Daily and Weekly. The Daily sub-tab SHALL default to today's date and support day-by-day navigation. The Weekly sub-tab shows the current week's insight. Both fetch from `GET /api/v1/advice` using the appropriate `adviceType` and `periodStart` parameters.

#### Scenario: Insights panel loads on page open
- **WHEN** a user navigates to the Coach page and opens the Insights tab
- **THEN** the Daily sub-tab is shown by default, displaying today's saved daily insight (if any)
- **THEN** the date navigator shows today's date label (e.g., "Today, May 26") with Previous and Next buttons

#### Scenario: No advice available for selected date
- **WHEN** the selected date has no saved daily insight
- **THEN** the daily panel shows a placeholder message indicating no insight exists for that date
- **THEN** a "Generate new insight" button is visible

#### Scenario: Insights fetch fails
- **WHEN** the advice endpoint returns an error
- **THEN** the insights panel shows a non-blocking error message and the chat panel remains fully functional

## ADDED Requirements

### Requirement: Daily insights support day-by-day navigation
The Daily insights sub-tab SHALL allow the user to navigate between days using Previous and Next buttons and a date picker, mirroring the Meal Log day-navigation pattern. Navigation SHALL be capped at today (no future dates).

#### Scenario: User navigates to the previous day
- **WHEN** the user clicks the "Previous" button on the Daily sub-tab
- **THEN** the selected date shifts back one day and the system fetches the saved insight for that date

#### Scenario: User navigates to the next day
- **WHEN** the user clicks the "Next" button and the selected date is before today
- **THEN** the selected date advances one day and the system fetches the saved insight for that date

#### Scenario: Next button is disabled on today
- **WHEN** the selected date is today
- **THEN** the "Next" button is disabled

#### Scenario: User picks a specific date via date picker
- **WHEN** the user opens the date picker and selects a past or current date
- **THEN** the selected date updates to the chosen date and the system fetches the saved insight for it
- **THEN** future dates are disabled in the date picker

#### Scenario: Generate and save insight for a past date
- **WHEN** the user navigates to a past date that has no saved insight and clicks "Generate new insight"
- **THEN** the system generates a preview using `periodStart` equal to the selected date
- **THEN** the user can save or discard the preview as usual

## REMOVED Requirements

### Requirement: History sub-tab
**Reason**: Replaced by the day-navigation view on the Daily sub-tab, which provides the same browsing capability with a cleaner UX.
**Migration**: Users browse past daily insights by navigating backwards using the Previous button or date picker on the Daily sub-tab.
