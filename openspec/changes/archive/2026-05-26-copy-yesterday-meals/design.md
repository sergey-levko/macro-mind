## Context

The meal log already supports creating individual logs and items. This feature adds a bulk-copy operation: given a target date, clone every meal log and its items from the previous calendar day for the same user. No schema changes are required — the copy is pure inserts into existing `meal_logs` and `meal_items` tables.

## Goals / Non-Goals

**Goals:**
- Single transactional endpoint copies all meal logs + items from `targetDate - 1` to `targetDate`
- Frontend shows a "Copy previous day" button in the meal log header; triggers a reload on success
- Empty source day returns a zero-copy success (no error)

**Non-Goals:**
- Copying from arbitrary source dates
- Deduplication — target date may already have logs; copies are always additive
- Adjusting time-of-day from source — all new logs use midnight UTC of target date
- Wizard / preview before copy — one-shot, user can delete unwanted logs afterwards

## Decisions

### 1. Endpoint: `POST /api/v1/meal-logs/copy-previous-day`

Body: `{ "date": "YYYY-MM-DD" }` (the target date to copy into).

Alternative considered: `POST /api/v1/meal-logs/copy?from=X&to=Y` for arbitrary-date copy. Rejected — generalising adds complexity with no stated need; keeping the contract narrow matches the UX intent.

### 2. Single `@Transactional` service method

The service fetches all source-day meal logs (via `MealLogRepository.findByUserIdAndDate()`), creates new `MealLog` entities for the target date, copies each `MealItem`, and flushes in one transaction. If anything fails the whole operation rolls back.

### 3. `loggedAt` set to midnight UTC of target date

Source timestamps are not preserved. The user already edits time-of-day inline after creation; starting at midnight is consistent with how the "+ Add meal" button works when no time is specified.

### 4. Response: `List<MealLogSummaryResponse>`

Returns the newly created summaries so the frontend can either append or simply reload. In practice the frontend calls `loadLogs()` after success, so the body is used only to check the count for the "nothing to copy" feedback.

### 5. Frontend: always-enabled button, empty-copy feedback via toast

The button is always visible in the header. If the previous day had no meals the endpoint returns an empty list and the frontend shows an inline "No meals on previous day" message rather than a toast. No extra pre-fetch needed.

## Risks / Trade-offs

- **Accidental double-copy** → User copies twice: target date gets duplicate logs. Acceptable per non-goals (copies are additive); user can delete extras.
- **Large meal history** → If a user has many meal logs on the source day, the single transaction copies all of them atomically. Given typical use (4 meal types, ~20 items total) this is negligible.
- **Source food deleted** → Not possible: `foods` are protected from deletion if referenced by a `meal_item` (`FoodInUseException`).

## Migration Plan

No schema changes. Standard feature deploy. Rollback: revert the PR.
