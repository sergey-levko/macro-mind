## Requirements

### Requirement: Copy all meals from the previous day into the current day
The system SHALL provide an endpoint that duplicates all meal logs (and their items) from a given source date into a target date for the authenticated user. If a meal log of the same type already exists on the target date, the items SHALL be merged into it rather than creating a duplicate log.

#### Scenario: Successful copy from previous day
- **WHEN** `POST /api/v1/meal-logs/copy` is called with a valid JWT, `sourceDate` set to the previous day, and `targetDate` set to today
- **THEN** the system duplicates all meal logs and their items from `sourceDate` into `targetDate`, and returns HTTP 200 with the list of created or updated meal logs

#### Scenario: Copy merges items into an existing meal log of the same type
- **WHEN** the target date already has a meal log of the same `mealType` as one being copied
- **THEN** the items from the source log are appended to the existing target log rather than creating a second log of the same type

#### Scenario: Source date has no meals
- **WHEN** `POST /api/v1/meal-logs/copy` is called and the user has no meal logs on `sourceDate`
- **THEN** the system returns HTTP 200 with an empty array and makes no changes to the target date

#### Scenario: Copy fails when sourceDate equals targetDate
- **WHEN** `POST /api/v1/meal-logs/copy` is called with `sourceDate` equal to `targetDate`
- **THEN** the system returns HTTP 400 Bad Request

#### Scenario: Copy fails when targetDate is in the future
- **WHEN** `POST /api/v1/meal-logs/copy` is called with a `targetDate` that is after today
- **THEN** the system returns HTTP 400 Bad Request
