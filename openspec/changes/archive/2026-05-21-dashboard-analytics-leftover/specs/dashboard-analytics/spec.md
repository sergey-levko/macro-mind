## ADDED Requirements

### Requirement: Get daily macro summary
The system SHALL return aggregated macro totals (calories, protein, carbs, fat) for all meals logged by the user on a given date, along with the user's nutritional goal targets for comparison.

#### Scenario: Successful daily summary with meals logged
- **WHEN** `GET /api/v1/dashboard/daily?date=<ISO-8601-date>` is called with a valid `X-User-Id` and a date on which meals exist
- **THEN** the system returns HTTP 200 with `date`, `totals` (calories, proteinG, carbsG, fatG), and `targets` (caloriesTarget, proteinG, carbsG, fatG from the user's nutritional goal)

#### Scenario: Daily summary with no meals logged
- **WHEN** `GET /api/v1/dashboard/daily?date=<date>` is called and the user has no meals logged on that date
- **THEN** the system returns HTTP 200 with `totals` all zero and `targets` populated (if goal exists)

#### Scenario: Daily summary with no nutritional goal set
- **WHEN** `GET /api/v1/dashboard/daily?date=<date>` is called and the user has no nutritional goal
- **THEN** the system returns HTTP 200 with `totals` computed normally and `targets` as null

#### Scenario: Daily summary missing date parameter
- **WHEN** `GET /api/v1/dashboard/daily` is called without a `date` query parameter
- **THEN** the system returns HTTP 400 Bad Request

### Requirement: Get weekly macro summary
The system SHALL return per-day macro totals for a 7-day window starting from a given date, plus aggregated weekly totals and targets scaled to 7 days.

#### Scenario: Successful weekly summary
- **WHEN** `GET /api/v1/dashboard/weekly?weekStart=<ISO-8601-date>` is called with a valid `X-User-Id`
- **THEN** the system returns HTTP 200 with `weekStart`, `days` (array of 7 daily entries each with `date` and `totals`), `weeklyTotals` (sum across all 7 days), and `weeklyTargets` (daily targets × 7)

#### Scenario: Weekly summary with partial data
- **WHEN** `GET /api/v1/dashboard/weekly?weekStart=<date>` is called and only some days in the window have meals
- **THEN** the system returns HTTP 200 with days that have no meals showing zero totals and days with meals showing correct aggregated totals

#### Scenario: Weekly summary with no nutritional goal
- **WHEN** `GET /api/v1/dashboard/weekly?weekStart=<date>` is called and the user has no nutritional goal
- **THEN** the system returns HTTP 200 with `weeklyTargets` as null

#### Scenario: Weekly summary missing weekStart parameter
- **WHEN** `GET /api/v1/dashboard/weekly` is called without a `weekStart` query parameter
- **THEN** the system returns HTTP 400 Bad Request

### Requirement: Get dashboard summary card
The system SHALL return a lightweight snapshot of today's macro intake vs. targets, expressed as raw totals and percentage completion per macro, for use in a summary card UI component.

#### Scenario: Successful summary card with goal set
- **WHEN** `GET /api/v1/dashboard/summary` is called with a valid `X-User-Id`
- **THEN** the system returns HTTP 200 with `date` (today), `totals`, `targets`, and `percentages` (caloriesPct, proteinPct, carbsPct, fatPct — each capped at 200 to avoid unbounded values)

#### Scenario: Summary card with no goal set
- **WHEN** `GET /api/v1/dashboard/summary` is called and the user has no nutritional goal
- **THEN** the system returns HTTP 200 with `totals` populated, `targets` as null, and `percentages` as null
