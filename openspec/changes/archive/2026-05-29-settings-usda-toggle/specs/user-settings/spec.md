## ADDED Requirements

### Requirement: Read user settings
The system SHALL expose `GET /api/v1/settings` returning the authenticated user's current settings as a JSON object.

#### Scenario: Settings returned for authenticated user
- **WHEN** `GET /api/v1/settings` is called with a valid JWT
- **THEN** the system returns HTTP 200 with `{ "usdaEnabled": true }` (or the current persisted value)

#### Scenario: Default value for new users
- **WHEN** a user registers and has never updated settings
- **THEN** `GET /api/v1/settings` returns `{ "usdaEnabled": true }`

### Requirement: Update user settings
The system SHALL expose `PUT /api/v1/settings` allowing the authenticated user to update their settings.

#### Scenario: Successful update of usdaEnabled
- **WHEN** `PUT /api/v1/settings` is called with `{ "usdaEnabled": false }` and a valid JWT
- **THEN** the system persists `usda_enabled = false` on the user record and returns HTTP 200 with `{ "usdaEnabled": false }`

#### Scenario: Disabling USDA takes effect immediately
- **WHEN** the user sets `usdaEnabled` to `false` and then calls `GET /api/v1/foods/usda-search`
- **THEN** the endpoint returns an empty list

#### Scenario: Re-enabling USDA restores search behavior
- **WHEN** the user sets `usdaEnabled` back to `true` and then calls `GET /api/v1/foods/usda-search`
- **THEN** the endpoint returns USDA results as normal

#### Scenario: Invalid body is rejected
- **WHEN** `PUT /api/v1/settings` is called with a missing or malformed body
- **THEN** the system returns HTTP 400 Bad Request
