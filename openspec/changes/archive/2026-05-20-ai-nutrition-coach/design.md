## Context

Spring AI with `spring-ai-starter-model-anthropic` is already on the classpath and configured to use `claude-sonnet-4-6` via `ANTHROPIC_API_KEY`. The `ai_advice` table is already in the database schema (id, user_id, advice_type ENUM, content TEXT, period_start DATE, created_at). All prerequisite data — user profile, meal logs, nutritional goals — is available via existing repositories.

This is the first slice to cross slice boundaries for data reads: the advice service aggregates from `user`, `meal`, and `goal` slices to build the AI prompt context.

## Goals / Non-Goals

**Goals:**
- Generate personalized nutrition advice via Claude for a given `adviceType` (DAILY/WEEKLY) and `periodStart` date
- Persist generated advice in `ai_advice` for later retrieval
- Retrieve a single advice record by ID
- List a user's advice records filtered by optional `adviceType` and `periodStart`
- Clean separation between prompt construction and AI invocation

**Non-Goals:**
- Streaming responses
- Regenerating or updating existing advice records
- Scheduled/automatic generation
- Frontend implementation

## Decisions

### 1. Spring AI `ChatClient` for prompt invocation
Use the auto-configured `ChatClient.Builder` bean to call Claude. This provides a fluent API for system + user prompt assembly and returns a `String` response. No need for structured output parsing — the advice is free-form text stored as `content TEXT`.

*Alternative considered:* Raw Anthropic REST calls via `RestClient` — more control but abandons the Spring AI abstraction already set up.

### 2. Prompt built from aggregated context in `AdviceService`
`AdviceService` queries `UserRepository`, `NutritionalGoalRepository`, and `MealLogRepository` directly (cross-slice repository reads, all public interfaces). It assembles a structured system prompt (user profile + goal) and a user prompt (period meal summary with macro totals vs targets) before calling the AI.

*Alternative considered:* Dedicated `AdviceContextBuilder` component — premature abstraction for a single consumer.

### 3. Idempotent generation — no duplicate guard
The spec allows a user to request advice for the same period multiple times; each call creates a new record. History is a feature, not a bug.

### 4. `adviceType` stored as VARCHAR with CHECK constraint, period_start as DATE
Matches the schema. `AdviceType` enum (`DAILY`, `WEEKLY`) mirrors the DB constraint. `periodStart` as `LocalDate` maps cleanly to `DATE`.

### 5. No Liquibase changeset needed
The `ai_advice` table was included in the project scaffold. No structural change required.

### 6. AI call excluded from integration tests
Integration tests stub `ChatClient` (using `@MockitoBean`) to avoid calling the real API and incurring latency/cost. Controller and service unit tests use Mockito throughout.

## Risks / Trade-offs

- **Claude API unavailability** → `AdviceService` lets Spring AI exceptions propagate; `GlobalExceptionHandler` catches generic `Exception` as 503 if needed, or the default 500 is acceptable for an alpha
- **Prompt quality** → advice content depends on prompt design; can be improved iteratively without changing the API contract
- **No goal set for user** → service returns HTTP 400 if user has no nutritional goal (can't generate meaningful advice without targets)
- **Large meal history** → for WEEKLY advice, up to 7 days of logs are summarised as macro totals per day to keep the prompt compact; individual items are not listed

## Migration Plan

No schema migration required. Deploy as a new vertical slice. `ANTHROPIC_API_KEY` must be set in the environment.

## Open Questions

None.
