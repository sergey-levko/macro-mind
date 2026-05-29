## ADDED Requirements

### Requirement: Settings page with USDA toggle
The system SHALL provide a Settings page at `/settings` accessible from the sidebar navigation, displaying a toggle to enable or disable the USDA food database integration.

#### Scenario: Settings page loads current preference
- **WHEN** the user navigates to `/settings`
- **THEN** the page calls `GET /api/v1/settings` and renders a toggle labeled "Use USDA food database" reflecting the current `usdaEnabled` value

#### Scenario: Toggling USDA off persists the change
- **WHEN** the user flips the "Use USDA food database" toggle to off
- **THEN** the app immediately calls `PUT /api/v1/settings` with `{ "usdaEnabled": false }`, and the toggle remains off on success

#### Scenario: Toggling USDA on persists the change
- **WHEN** the user flips the "Use USDA food database" toggle to on
- **THEN** the app immediately calls `PUT /api/v1/settings` with `{ "usdaEnabled": true }`, and the toggle remains on on success

#### Scenario: Toggle reverts on API failure
- **WHEN** `PUT /api/v1/settings` returns a non-2xx response
- **THEN** the toggle reverts to its previous value and an error toast is displayed

#### Scenario: Toggle shows loading state during save
- **WHEN** the `PUT /api/v1/settings` request is in-flight
- **THEN** the toggle is disabled until the request resolves

### Requirement: Settings nav link in sidebar
The system SHALL add a Settings link to the sidebar navigation.

#### Scenario: Settings link is visible in the sidebar
- **WHEN** the user is authenticated and views any page
- **THEN** the sidebar displays a "Settings" navigation link

#### Scenario: Settings link navigates to /settings
- **WHEN** the user clicks the Settings link
- **THEN** the browser navigates to `/settings` and the link is highlighted as active
