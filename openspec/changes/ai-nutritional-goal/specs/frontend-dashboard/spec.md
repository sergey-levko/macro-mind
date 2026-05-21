## ADDED Requirements

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
