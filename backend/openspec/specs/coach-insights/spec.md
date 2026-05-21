## ADDED Requirements

### Requirement: Coach page displays proactive daily and weekly insights
The Coach page SHALL display an insights panel that shows AI-generated daily advice and weekly advice by fetching from the existing `GET /api/v1/advice` endpoint with `type=DAILY` and `type=WEEKLY`.

#### Scenario: Insights load on page open
- **WHEN** a user navigates to the Coach page
- **THEN** both daily and weekly insights are fetched and displayed in the insights panel

#### Scenario: No advice available yet
- **WHEN** the advice endpoint returns an empty list for a given type
- **THEN** the insights panel shows a placeholder message (e.g., "No insights yet — log some meals to get started")

#### Scenario: Insights fetch fails
- **WHEN** the advice endpoint returns an error
- **THEN** the insights panel shows a non-blocking error message and the chat panel remains fully functional

### Requirement: Coach tab is accessible from sidebar navigation
The sidebar navigation SHALL include a Coach entry that routes to the Coach page.

#### Scenario: User navigates to Coach tab
- **WHEN** a user clicks "Coach" in the sidebar
- **THEN** the Coach page is displayed with both the chat panel and insights panel visible
