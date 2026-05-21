## ADDED Requirements

### Requirement: User registration and identity persistence
The system SHALL allow a new visitor to register a user profile and persist their UUID in localStorage so subsequent visits skip registration.

#### Scenario: First visit redirects to registration
- **WHEN** a user visits any page and no `macromind_user_id` key exists in localStorage
- **THEN** the app redirects to `/register`

#### Scenario: Successful registration stores UUID and redirects to dashboard
- **WHEN** the user submits the registration form with valid name, email, age, weight, height, and goal type
- **THEN** the app calls `POST /api/v1/users`, stores the returned `id` under `macromind_user_id` in localStorage, and navigates to `/dashboard`

#### Scenario: Returning user skips registration
- **WHEN** a user visits the app and `macromind_user_id` exists in localStorage
- **THEN** the app does NOT redirect to `/register` and renders the requested page directly

#### Scenario: Registration form validation
- **WHEN** the user submits the form with missing required fields
- **THEN** the form displays inline validation errors and does not submit

### Requirement: App shell with persistent navigation
The system SHALL render a persistent layout with navigation links accessible on all authenticated pages.

#### Scenario: Navigation renders on all main pages
- **WHEN** a logged-in user is on any page other than `/register`
- **THEN** the layout renders a sidebar/topbar with links to Dashboard (`/dashboard`) and Meal Log (`/meal-log`)

#### Scenario: Active route is visually highlighted in navigation
- **WHEN** the user is on a page matching a navigation link
- **THEN** that navigation link is styled as active

### Requirement: API client injects user identity header
The system SHALL inject the `X-User-Id` header on every API request automatically.

#### Scenario: All API calls include X-User-Id header
- **WHEN** any API call is made while a `macromind_user_id` exists in localStorage
- **THEN** the request includes `X-User-Id: <uuid>` in its headers

#### Scenario: API call without identity throws before fetching
- **WHEN** an API call is triggered but no `macromind_user_id` exists in localStorage
- **THEN** the API client throws an error without making the network request
