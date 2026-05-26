## ADDED Requirements

### Requirement: New users are guided to generate nutritional goals after registration
The system SHALL display a post-registration onboarding page at `/onboarding` that prompts the user to generate their AI nutritional goals before using the main app. The page SHALL be protected (requires authentication) and SHALL redirect unauthenticated visitors to `/login`.

#### Scenario: New user is redirected to onboarding after registration
- **WHEN** a user successfully completes the registration form
- **THEN** the system redirects to `/onboarding` instead of `/dashboard`

#### Scenario: Onboarding page displays user profile summary
- **WHEN** an authenticated user lands on `/onboarding`
- **THEN** the page shows a welcome message personalised with the user's name and a summary of their profile (goal type)

#### Scenario: User generates nutritional goals
- **WHEN** the user clicks "Generate my macros"
- **THEN** the system calls `POST /api/v1/goals/generate`, shows a loading spinner with the label "Calculating your macros…", and on success redirects to `/dashboard`

#### Scenario: Goal generation fails
- **WHEN** `POST /api/v1/goals/generate` returns an error
- **THEN** the system displays an inline error message and a "Try again" button; the skip link remains visible

#### Scenario: User skips onboarding
- **WHEN** the user clicks "Skip for now"
- **THEN** the system redirects to `/dashboard` without generating goals

### Requirement: Stale pre-auth registration page is removed
The system SHALL NOT expose a standalone registration page at `/register` backed by the old `POST /api/v1/users` endpoint. The `Register.tsx` component SHALL be deleted.

#### Scenario: Direct navigation to /register
- **WHEN** a user navigates to `/register`
- **THEN** the system redirects to `/` (which redirects to `/dashboard` or `/login` based on auth state)
