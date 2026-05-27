## Requirements

### Requirement: System auto-generates daily insights overnight
The system SHALL automatically generate a DAILY insight for every active user who logged at least one meal on the previous calendar day (UTC) but has no saved DAILY insight for that date. An active user is defined as one who has logged at least one meal in the past 30 days. The job SHALL run nightly at approximately 02:00 UTC via a configurable cron expression.

#### Scenario: Daily job generates insight for eligible user
- **WHEN** the nightly scheduled job runs at ~02:00 UTC
- **THEN** the system identifies users who had at least one meal log on the previous UTC calendar day but no saved DAILY insight for that date
- **THEN** a DAILY insight is generated and saved asynchronously for each eligible user

#### Scenario: Daily job skips user with existing insight
- **WHEN** the nightly scheduled job runs and a user already has a saved DAILY insight for the previous day
- **THEN** the system does NOT generate a duplicate insight for that user

#### Scenario: Daily job skips user with no meals for the day
- **WHEN** the nightly scheduled job runs and a user had no meal logs on the previous calendar day
- **THEN** no DAILY insight is generated for that user for that date

#### Scenario: Daily job skips inactive users
- **WHEN** the nightly scheduled job runs and a user has had no meal logs in the past 30 days
- **THEN** that user is excluded from insight generation entirely

### Requirement: System auto-generates weekly insights at end of week
The system SHALL automatically generate a WEEKLY insight for every active user at the end of each Sunday (UTC), covering the Monday–Sunday ISO week. The job SHALL run at approximately 23:30 UTC every Sunday via a configurable cron expression. `periodStart` SHALL be set to the Monday of the ending week.

#### Scenario: Weekly job generates insight for eligible user
- **WHEN** the weekly scheduled job runs on Sunday at ~23:30 UTC
- **THEN** the system identifies active users who had at least one meal log during the Monday–Sunday week ending that day but have no saved WEEKLY insight with `periodStart` equal to that Monday
- **THEN** a WEEKLY insight is generated and saved asynchronously for each eligible user

#### Scenario: Weekly job skips user with existing weekly insight
- **WHEN** the weekly scheduled job runs and a user already has a saved WEEKLY insight for the current week's Monday
- **THEN** no duplicate insight is generated for that user

#### Scenario: Weekly job skips user with no meals this week
- **WHEN** the weekly scheduled job runs and a user had no meal logs during the current Mon–Sun week
- **THEN** no WEEKLY insight is generated for that user

### Requirement: Scheduled insight generation is fault-tolerant per user
The scheduler SHALL catch and log exceptions on a per-user basis so that a single failure (e.g., Claude API error, missing nutritional goal) does not abort generation for remaining users.

#### Scenario: Per-user failure is isolated
- **WHEN** the scheduler processes a batch of users and one user's insight generation throws an exception
- **THEN** the error is logged with the user ID and reason
- **THEN** the scheduler continues processing the remaining users in the batch

### Requirement: Scheduled insight cron expressions are configurable
The cron expressions controlling both jobs SHALL be externalised to `application.properties` so they can be overridden per environment without code changes.

#### Scenario: Custom cron expression is applied
- **WHEN** `insights.schedule.daily-cron` or `insights.schedule.weekly-cron` is set in application properties
- **THEN** the scheduler uses the configured cron expression instead of the default
