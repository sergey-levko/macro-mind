## 1. Repository Query

- [x] 1.1 Add `findDistinctUserIdsByLoggedAtBetween(Instant start, Instant end)` JPQL query to `MealLogRepository` returning `List<UUID>`; commit

## 2. Scheduling Infrastructure

- [x] 2.1 Create `SchedulingConfig` (`@Configuration @EnableScheduling`) in the `advice` package; commit

## 3. Scheduler Implementation

- [x] 3.1 Create `InsightScheduler` (`@Component`) with daily job method: resolve yesterday's date range, find active users with meals yesterday and no existing DAILY insight, call `generateAdvice` per user with try/catch logging; cron from `insights.schedule.daily-cron` property; commit
- [x] 3.2 Add weekly job method to `InsightScheduler`: resolve current week's Monday, find active users with meals Mon–Sun and no existing WEEKLY insight for that Monday, call `generateAdvice` per user with try/catch logging; cron from `insights.schedule.weekly-cron` property; commit
- [x] 3.3 Add default cron values to `application.properties`: `insights.schedule.daily-cron=0 0 2 * * *` and `insights.schedule.weekly-cron=0 30 23 * * SUN`; commit

## 4. Tests

- [x] 4.1 Unit test `InsightScheduler` with Mockito: daily job skips user with existing insight, skips user with no meals, processes eligible user, isolates per-user failures; commit
- [x] 4.2 Unit test weekly job: skips existing insight, skips no-meals, processes eligible user, correct `periodStart` is Monday of current week; commit
