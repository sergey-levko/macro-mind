## MODIFIED Requirements

### Requirement: Daily macro summary card
The system SHALL display the selected date's macro intake and percentage completion against the user's nutritional goal targets. The selected date defaults to today and can be changed via the date navigator.

#### Scenario: Summary card renders with goal set
- **WHEN** the user navigates to `/dashboard` and a nutritional goal exists
- **THEN** the page displays four macro progress indicators (calories, protein, carbs, fat) each showing consumed amount, target, and percentage completion for the selected date

#### Scenario: Summary card renders with no goal set
- **WHEN** the user navigates to `/dashboard` and no nutritional goal is set
- **THEN** the summary card displays totals only for the selected date, with a prompt to set a nutritional goal

#### Scenario: Percentage capped display
- **WHEN** a macro percentage from the API is greater than 100
- **THEN** the progress indicator shows it as over-target (e.g. red fill) rather than clipping the value

#### Scenario: Summary card updates on date change
- **WHEN** the user navigates to a different date via the date navigator
- **THEN** the summary card re-fetches `GET /api/v1/dashboard/daily?date=<selected-date>` and displays the macro totals for that date

### Requirement: Weekly macro bar chart
The system SHALL display a bar chart showing daily calorie intake across the week containing the selected date.

#### Scenario: Weekly chart renders 7 day bars
- **WHEN** the user views the dashboard
- **THEN** a bar chart shows 7 bars — one per day starting from Monday of the week containing the selected date — with each bar representing that day's total calorie intake

#### Scenario: Days with no meals show zero bar
- **WHEN** a day in the weekly window has no meal logs
- **THEN** its bar has height zero and is not hidden or omitted

#### Scenario: Weekly chart updates when selected date crosses a week boundary
- **WHEN** the user navigates to a date in a different week
- **THEN** the chart re-fetches `GET /api/v1/dashboard/weekly?weekStart=<monday-of-selected-week>` and shows the new week's data

## ADDED Requirements

### Requirement: Dashboard date navigator
The dashboard SHALL include a date navigator that allows the user to move between days to review past macro performance.

#### Scenario: Dashboard defaults to today
- **WHEN** the user navigates to `/dashboard`
- **THEN** the selected date is today and both the summary card and weekly chart reflect today's data

#### Scenario: User navigates to the previous day
- **WHEN** the user clicks the Previous button in the date navigator
- **THEN** the selected date moves back one day and both the summary card and weekly chart re-fetch for the new date

#### Scenario: User navigates to the next day
- **WHEN** the user clicks the Next button and the selected date is before today
- **THEN** the selected date advances one day and both the summary card and weekly chart re-fetch for the new date

#### Scenario: Next button disabled on today
- **WHEN** the selected date equals today
- **THEN** the Next button is disabled and cannot be clicked

#### Scenario: Today shortcut resets to current date
- **WHEN** the user is viewing a past date and clicks the Today button
- **THEN** the selected date resets to today and both the summary card and weekly chart re-fetch
