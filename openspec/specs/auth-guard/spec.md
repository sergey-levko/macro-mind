### Requirement: Unauthenticated users see login screen
The frontend application SHALL redirect users to a login/register screen when no valid JWT is present in localStorage.

#### Scenario: No token on app load
- **WHEN** the app loads and localStorage contains no JWT token
- **THEN** the user sees the login/register screen and cannot access any other page

#### Scenario: Token present on app load
- **WHEN** the app loads and localStorage contains a JWT token
- **THEN** the user is taken directly to the authenticated app (dashboard)

### Requirement: Protected route wrapper
All authenticated pages SHALL be wrapped in a route guard component that checks for a valid token before rendering.

#### Scenario: Authenticated user navigates to protected route
- **WHEN** an authenticated user navigates to any protected route (e.g. `/dashboard`, `/meal-log`)
- **THEN** the page renders normally

#### Scenario: Unauthenticated user navigates to protected route
- **WHEN** a user with no token navigates directly to a protected route URL
- **THEN** the app redirects to the login screen

### Requirement: Token attached to all API requests
The frontend API client SHALL attach `Authorization: Bearer <token>` to every outgoing request when a token is present.

#### Scenario: Authenticated API call
- **WHEN** any API call is made while the user is logged in
- **THEN** the request includes the `Authorization: Bearer <token>` header

### Requirement: Logout clears session
The system SHALL allow the user to log out, which clears the JWT from localStorage and returns the user to the login screen.

#### Scenario: User logs out
- **WHEN** the user clicks the logout button
- **THEN** the JWT is removed from localStorage and the user is redirected to the login screen

#### Scenario: 401 response clears session
- **WHEN** any API call returns HTTP 401
- **THEN** the token is cleared from localStorage and the user is redirected to the login screen
