## Why

Users must manually click "Generate new insight" to get daily and weekly coaching insights. This makes the Coach tab feel reactive rather than proactive — users who forget to generate insights miss out on personalized guidance. Automating generation overnight and at week's end ensures insights are ready when users open the app.

## What Changes

- A Spring `@Scheduled` job runs nightly (e.g. 02:00 UTC) and generates a DAILY insight for every active user who had at least one meal logged the previous day but has no saved insight for that date yet.
- A second scheduled job runs Sunday night (e.g. 23:30 UTC) and generates a WEEKLY insight for every active user who has meal data for the ending week but no saved insight for that week yet.
- "Active user" is defined as any user who has logged at least one meal in the past 30 days.
- Both jobs are idempotent — they skip users who already have an insight for the target period.
- No new API endpoints are added; jobs reuse the existing AI advice generation logic in `AiAdviceService`.
- Scheduling is configurable via `application.properties` cron expressions.

## Capabilities

### New Capabilities
- `scheduled-insights`: Background scheduler that auto-generates daily and weekly AI insights for active users

### Modified Capabilities

## Impact

- **Backend**: New `InsightScheduler` component in the `ai` slice. Reuses `AiAdviceService.generateAndSave(userId, adviceType, periodStart)` (or equivalent internal method). Reads from `meal_logs` and `ai_advice` tables. No schema changes needed.
- **Dependencies**: Spring `@EnableScheduling` must be activated on the main application class (or a config class).
- **Non-goals**: Push notifications to users, per-user timezone scheduling, configuring schedule from the UI, backfilling historical insights.
