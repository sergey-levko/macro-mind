## Context

MacroMind has a fully working AI advice system (`com.epam.macromind.advice`) that generates daily and weekly nutrition insights using Spring AI `ChatClient` against `claude-sonnet-4-6`. This system is never called from the UI. The app also has no chat or conversational interface. Users see macro data but receive no coaching or guidance.

The change adds a Coach tab that exposes both a conversation interface and auto-generated proactive insights. The backend already has all required dependencies (Spring AI, `ChatClient` bean, `MealLogRepository`).

## Goals / Non-Goals

**Goals:**
- Add `POST /api/v1/chat` endpoint: accepts a user message, injects recent meal log data as context, returns an AI reply
- Add a Coach tab to the frontend with a chat panel and an insights panel
- Display daily and weekly insights on the Coach page by wiring `GET /api/v1/advice`

**Non-Goals:**
- Persistent conversation history — each message is stateless (consistent with the existing advice pattern)
- User authentication changes — `X-User-Id` header pattern remains unchanged
- Streaming responses — standard request/response only
- Modifying the existing advice endpoints or advice generation logic

## Decisions

### Stateless chat (no history)
**Decision:** Each `POST /api/v1/chat` request is independent; no conversation turns are stored or sent to the model.  
**Rationale:** The existing advice system is stateless, matching the Spring AI usage pattern already in the codebase. Storing conversation history adds schema changes, memory pressure, and complexity not justified by the initial use case. Users asking follow-up questions can repeat context in their message.  
**Alternative considered:** Store last N messages in a session/DB and include them in the prompt. Rejected — over-engineering for a first iteration.

### Meal log context window: last 7 days
**Decision:** Inject the user's meal logs from the past 7 days into the system prompt for each chat request.  
**Rationale:** 7 days provides enough pattern data for meaningful nutrition coaching without making prompts excessively large. Mirrors the `WEEKLY` advice window already used in `AdvicePromptBuilder`.  
**Alternative considered:** Last 3 days (less context) or 30 days (token-expensive, diminishing returns).

### Reuse existing advice endpoint for insights
**Decision:** The frontend fetches `GET /api/v1/advice?userId=...&type=DAILY` and `GET /api/v1/advice?userId=...&type=WEEKLY` to populate the insights panel. No new backend endpoint is needed.  
**Rationale:** The endpoint already works end-to-end; adding another layer would be pure duplication.

### New `coach` package on the backend
**Decision:** Place `CoachController` and `CoachService` in `com.epam.macromind.coach`.  
**Rationale:** Follows the existing per-feature package structure (meal, food, dashboard, advice). Chat is conceptually different from advice (request-driven vs auto-generated), so a new package keeps concerns separate.

## Risks / Trade-offs

- **Token cost per chat message** — injecting 7 days of meal data into every message is expensive. Mitigation: summarize meals (food name + macros only) rather than full JSON blobs; keep the context lean.
- **Model latency** — chat responses from Claude can take 2–5 seconds. Mitigation: show a loading spinner in the UI; no timeout changes needed on backend (Spring AI default is sufficient).
- **No history means no follow-ups** — users cannot reference previous turns. Mitigation: acceptable for v1; document as a known limitation.
