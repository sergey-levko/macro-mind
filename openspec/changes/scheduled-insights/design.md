## Context

The `advice` slice already contains `AiAdviceService` (user-triggered generation), `AsyncAdviceGenerator` (non-blocking Claude API calls), `AdvicePromptBuilder` (prompt assembly from meal data), and `AiAdviceRepository`. The scheduler will live in the same package to access package-private service and repository without visibility changes.

## Goals / Non-Goals

**Goals:**
- Auto-generate DAILY insights nightly for eligible users (had meals yesterday, no saved insight for that date)
- Auto-generate WEEKLY insights Sunday night for eligible users (had meals this week, no saved insight for this week)
- Idempotent — re-running the job never creates duplicates
- Per-user failure isolation — one failed Claude call does not abort the batch

**Non-Goals:**
- Push notifications to users
- Per-user timezone scheduling (UTC only)
- Backfilling historical insights
- Configuring schedule from the UI
- New API endpoints

## Decisions

**Decision 1: Scheduler lives in the `advice` package**
`AiAdviceService`, `AiAdviceRepository`, and `AsyncAdviceGenerator` are package-private. Placing `InsightScheduler` in the same package avoids making them public. Alternative (separate `scheduler` package) would require widening visibility across a vertical slice, which is against the project's conventions.

**Decision 2: Reuse `AiAdviceService.generateAdvice()` after a pre-check**
`generateAdvice(preview=false, content=null)` creates an `AiAdvice` entity and delegates async completion to `AsyncAdviceGenerator`. The scheduler performs its own idempotency check first (skip if insight already exists), so the delete-on-overwrite inside `generateAdvice` is never triggered. This avoids duplicating the generation pipeline.

**Decision 3: Active user query via new JPQL on `MealLogRepository`**
Add `findDistinctUserIdsByLoggedAtAfter(Instant threshold)` to `MealLogRepository` — a `SELECT DISTINCT m.userId` query scoped to the past 30 days. Alternative (iterating all users) wastes DB and API resources on inactive accounts.

**Decision 4: `@EnableScheduling` on a dedicated config class**
A new `SchedulingConfig` class (same `advice` package) annotated with `@Configuration @EnableScheduling` keeps the main application class clean. Cron expressions are externalised to `application.properties` with sensible defaults.

**Decision 5: Cron defaults**
- Daily: `0 0 2 * * *` (02:00 UTC every day) — well after midnight UTC so previous day's meals are settled
- Weekly: `0 30 23 * * SUN` (23:30 UTC every Sunday) — end of the last day of the ISO week

## Risks / Trade-offs

- **API cost at scale**: Every eligible user triggers one Claude API call. For large user bases this could be expensive. Mitigation: The active-user filter (30-day window) limits scope; future work can add opt-out or batching.
- **Async completion race**: `AsyncAdviceGenerator` writes back asynchronously. If the app restarts mid-generation the `AiAdvice` row stays in `PENDING` state. Mitigation: Pre-existing behaviour — same as user-triggered generation; no new risk introduced.
- **Sunday border condition**: Weekly job runs at 23:30 UTC Sunday. `periodStart` is set to the Monday of that week (`LocalDate.now(ZoneOffset.UTC).with(DayOfWeek.MONDAY)`). Meals logged after 23:30 on Sunday are missed until the user triggers manually. Acceptable trade-off given non-goals.
