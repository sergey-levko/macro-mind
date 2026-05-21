## ADDED Requirements

### Requirement: Daily macro summary card
The system SHALL display today's macro intake and percentage completion against the user's nutritional goal targets.

#### Scenario: Summary card renders with goal set
- **WHEN** the user navigates to `/dashboard` and a nutritional goal exists
- **THEN** the page displays four macro progress indicators (calories, protein, carbs, fat) each showing consumed amount, target, and percentage completion

#### Scenario: Summary card renders with no goal set
- **WHEN** the user navigates to `/dashboard` and no nutritional goal is set
- **THEN** the summary card displays totals only, with a prompt to set a nutritional goal

#### Scenario: Percentage capped display
- **WHEN** a macro percentage from the API is greater than 100
- **THEN** the progress indicator shows it as over-target (e.g. red fill) rather than clipping the value

### Requirement: Weekly macro bar chart
The system SHALL display a bar chart showing daily calorie intake across the current week.

#### Scenario: Weekly chart renders 7 day bars
- **WHEN** the user views the dashboard
- **THEN** a bar chart shows 7 bars — one per day starting from Monday of the current week — with each bar representing that day's total calorie intake

#### Scenario: Days with no meals show zero bar
- **WHEN** a day in the weekly window has no meal logs
- **THEN** its bar has height zero and is not hidden or omitted

### Requirement: Inline nutritional goal setting
The system SHALL allow the user to set or update their nutritional goal directly from the dashboard.

#### Scenario: Goal form appears when no goal is set
- **WHEN** the user views the dashboard and has no nutritional goal
- **THEN** a goal-setting form is visible with fields for calorie target, protein, carbs, and fat

#### Scenario: Existing goal values are pre-filled
- **WHEN** the user opens the goal form and a goal already exists
- **THEN** the form fields are pre-filled with the current target values

#### Scenario: Saving goal updates the summary card
- **WHEN** the user submits the goal form with valid values
- **THEN** the app calls `PUT /api/v1/nutritional-goals` and the summary card re-renders with the new targets without a full page reload

### Requirement: Generate nutritional goal with AI
The system SHALL allow the user to generate a nutritional goal suggestion from the dashboard goal form using Claude.

#### Scenario: Generate with AI button pre-fills the form
- **WHEN** the user clicks "Generate with AI" in the goal form
- **THEN** the app calls `POST /api/v1/nutritional-goals/generate` and pre-fills the `caloriesTarget`, `proteinG`, `carbsG`, and `fatG` fields with the returned values

#### Scenario: Loading state while generation is in flight
- **WHEN** the AI generation request is in flight
- **THEN** the "Generate with AI" button shows a loading indicator and is disabled until the response arrives

#### Scenario: Error state when generation fails
- **WHEN** the `POST /api/v1/nutritional-goals/generate` call fails
- **THEN** the app displays the message "Generation failed, please try again" and the form fields remain unchanged

#### Scenario: User must explicitly save the suggestion
- **WHEN** the form fields are pre-filled with the AI suggestion
- **THEN** the goal is not persisted until the user explicitly clicks "Save"
