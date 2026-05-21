## Requirements

### Requirement: View and edit user profile
The system SHALL provide a Profile page where the user can view their current profile data and submit updates.

#### Scenario: Profile page loads current user data
- **WHEN** the user navigates to `/profile`
- **THEN** the page fetches `GET /api/v1/users/{userId}` and displays the current values for `name`, `email`, `age`, `weight_kg`, `height_cm`, and `goal_type`

#### Scenario: Email field is read-only
- **WHEN** the profile form is displayed
- **THEN** the `email` field is shown as read-only and cannot be edited

#### Scenario: Successful profile update
- **WHEN** the user modifies one or more editable fields and clicks Save
- **THEN** the app calls `PUT /api/v1/users/{userId}` with the updated values, displays a success toast "Profile updated", and re-renders the form with the saved data

#### Scenario: Save fails with server error
- **WHEN** `PUT /api/v1/users/{userId}` returns a non-2xx response
- **THEN** the app displays an error toast "Failed to update profile, please try again" and the form retains the entered values

#### Scenario: Loading state while saving
- **WHEN** the PUT request is in flight
- **THEN** the Save button is disabled and shows a loading label

### Requirement: Profile nav link in sidebar
The system SHALL add a Profile link to the sidebar navigation.

#### Scenario: Profile link is visible in the sidebar
- **WHEN** the user is logged in and views any page
- **THEN** the sidebar displays a "Profile" navigation link

#### Scenario: Profile link navigates to /profile
- **WHEN** the user clicks the Profile link
- **THEN** the browser navigates to `/profile` and the link is highlighted as active
