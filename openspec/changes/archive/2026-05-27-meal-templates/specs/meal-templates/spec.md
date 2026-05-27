## ADDED Requirements

### Requirement: Save meals as a named template
The system SHALL allow the user to save all meal items logged on a given date as a named template for reuse on future dates.

#### Scenario: Save today's meals as a template
- **WHEN** the user provides a template name and calls `POST /api/v1/meal-templates` with `{ name, date }`
- **THEN** the system reads all `meal_items` from `meal_logs` on that date for the authenticated user, creates a `meal_templates` record and corresponding `meal_template_items` records, and returns `201 Created` with the new template including its id, name, item count, and created_at

#### Scenario: Save fails when no meals are logged on the date
- **WHEN** the user calls `POST /api/v1/meal-templates` with a date that has no meal logs
- **THEN** the system returns `400 Bad Request` with a message indicating no meals were found for that date

#### Scenario: Template name must not be empty
- **WHEN** the user calls `POST /api/v1/meal-templates` with a blank or missing name
- **THEN** the system returns `400 Bad Request`

### Requirement: List saved templates
The system SHALL return all templates saved by the authenticated user.

#### Scenario: List returns all user templates
- **WHEN** the user calls `GET /api/v1/meal-templates`
- **THEN** the system returns `200 OK` with an array of templates, each including id, name, created_at, item count, and macro totals (calories, protein, carbs, fat computed from current food data)

#### Scenario: List is empty when no templates exist
- **WHEN** the user has no saved templates and calls `GET /api/v1/meal-templates`
- **THEN** the system returns `200 OK` with an empty array

### Requirement: Apply a template to a date
The system SHALL allow the user to apply a saved template to a target date, creating meal logs and items for each entry in the template.

#### Scenario: Apply creates meal logs grouped by meal type
- **WHEN** the user calls `POST /api/v1/meal-templates/{id}/apply` with `{ date }`
- **THEN** the system creates one `meal_log` per distinct meal type present in the template, adds all corresponding `meal_items` to each log, and returns `200 OK` with the list of created `MealLogSummary` objects

#### Scenario: Applying the same template twice creates duplicate logs
- **WHEN** the user applies the same template to the same date a second time
- **THEN** the system creates a new set of meal logs and items (no deduplication) and returns `200 OK`

#### Scenario: Apply fails for template belonging to another user
- **WHEN** the user calls `POST /api/v1/meal-templates/{id}/apply` with an id that belongs to a different user
- **THEN** the system returns `404 Not Found`

### Requirement: Delete a template
The system SHALL allow the user to delete a saved template.

#### Scenario: Delete removes the template and its items
- **WHEN** the user calls `DELETE /api/v1/meal-templates/{id}` for a template they own
- **THEN** the system deletes the `meal_templates` record (cascading to `meal_template_items`) and returns `204 No Content`

#### Scenario: Delete fails for unknown template
- **WHEN** the user calls `DELETE /api/v1/meal-templates/{id}` with an id that does not exist or belongs to another user
- **THEN** the system returns `404 Not Found`
