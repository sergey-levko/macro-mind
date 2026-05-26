## ADDED Requirements

### Requirement: Render errors are caught per route and display a recovery UI
The system SHALL wrap each protected route with a React error boundary so that an unhandled render-phase error in one route does not unmount the navigation sidebar or other routes. The boundary SHALL display a styled fallback UI that allows the user to recover without a full page reload.

#### Scenario: Render error on a protected route
- **WHEN** a render-phase error is thrown inside a protected route (e.g. Dashboard, Meal Log, Coach, Foods, Profile)
- **THEN** the error boundary catches it, replaces the route content with a fallback UI, and leaves the navigation sidebar intact so the user can navigate to other routes

#### Scenario: User resets the error boundary in place
- **WHEN** the user clicks the "Reload section" button on the fallback UI
- **THEN** the boundary resets and attempts to re-render the route from scratch

#### Scenario: User navigates away from the errored route
- **WHEN** the user navigates to a different route via the sidebar while a route is in the error state
- **THEN** the error boundary resets automatically and the new route renders normally

#### Scenario: User escapes to the dashboard from the fallback UI
- **WHEN** the user clicks "Go to dashboard" on the fallback UI
- **THEN** the system navigates to `/dashboard`

### Requirement: A root-level boundary provides a last-resort catch-all
The system SHALL have a root-level error boundary in `App.tsx` that catches any render error not caught by a per-route boundary (e.g. errors in `LoginPage`, `Onboarding`, or the `Layout` component itself). The root fallback SHALL offer a full-page reload button.

#### Scenario: Render error outside a per-route boundary
- **WHEN** a render-phase error is thrown in a component not wrapped by a per-route boundary (e.g. LoginPage, Onboarding)
- **THEN** the root boundary catches it and displays a full-page fallback with a reload button

#### Scenario: Root boundary does not interfere with normal operation
- **WHEN** no render error occurs
- **THEN** the root boundary renders its children transparently with no visible difference to the user
