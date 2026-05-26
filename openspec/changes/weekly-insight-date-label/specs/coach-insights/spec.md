## MODIFIED Requirements

### Requirement: Coach page displays proactive daily and weekly insights
The Coach page SHALL display an insights panel with two sub-tabs: Daily and Weekly. The Daily sub-tab SHALL default to today's date and support day-by-day navigation. The Weekly sub-tab shows the current week's insight. Both fetch from `GET /api/v1/advice` using the appropriate `adviceType` and `periodStart` parameters.

#### Scenario: Saved weekly insight shows a formatted week range label
- **WHEN** a saved weekly insight is displayed in the insights panel
- **THEN** the date label shows a human-readable week range (e.g. "May 19 – 25" or "This week (May 19 – 25)") instead of a raw ISO date string
