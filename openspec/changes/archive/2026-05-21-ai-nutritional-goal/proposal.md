## Why

Setting a nutritional goal requires users to manually research calorie and macro targets for their body and fitness objective. Claude already has the user's profile (age, weight, height, goal type) — it can generate a personalized, evidence-based goal recommendation in one click, removing friction and improving accuracy.

## What Changes

- New backend endpoint `POST /api/v1/nutritional-goals/generate` — calls Claude with the user's profile and returns a suggested `NutritionalGoal` (not yet saved)
- New frontend button "Generate with AI" on the goal form in the Dashboard — calls the endpoint, pre-fills the form fields with Claude's suggestion, user can review and save

## Non-goals

- Automatically saving the AI-generated goal without user confirmation
- Generating meal plans or food recommendations (separate `ai-advice` capability)
- Regenerating goals on a schedule

## Capabilities

### New Capabilities

- `ai-nutritional-goal`: Claude-powered nutritional goal suggestion — given a user profile, Claude returns recommended calorie and macro targets that the user can accept or edit before saving

### Modified Capabilities

- `nutritional-goals`: Adds a new `POST /api/v1/nutritional-goals/generate` endpoint alongside the existing `PUT /api/v1/nutritional-goals`; the GET and PUT behaviour is unchanged
- `frontend-dashboard`: The goal form gains a "Generate with AI" button that pre-fills fields from the AI suggestion

## Impact

- **Backend**: New method in `NutritionalGoalService` calling Spring AI / Claude; new controller endpoint in `NutritionalGoalController`; new request/response DTOs
- **Frontend**: `src/pages/Dashboard.tsx` — `GoalForm` gains "Generate with AI" button with loading state
- **API endpoints added**: `POST /api/v1/nutritional-goals/generate`
- **Database tables**: `nutritional_goals` (read for context), `users` (read for profile); no schema changes
- **Dependencies**: Spring AI (already present), Claude API key (already configured)
