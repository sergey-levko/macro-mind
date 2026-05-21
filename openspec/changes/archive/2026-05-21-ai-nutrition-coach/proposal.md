
## Why

MacroMind tracks macros but offers no guidance on what to do with that data. Users currently receive no feedback, recommendations, or conversation — the advice backend (`POST/GET /api/v1/advice`) exists and works but is completely unused by the frontend. Adding a Coach tab surfaces AI-powered support and makes the advice system visible for the first time.

## What Changes

- Add a **Coach** page/tab to the frontend sidebar navigation
- Build a **chat interface** on the Coach page: users can ask nutrition questions and receive AI-powered responses
- Build a **proactive insights panel** on the Coach page: displays auto-generated daily/weekly advice pulled from the existing advice endpoints
- Add a backend **chat endpoint** (`POST /api/v1/chat`) for conversational, context-aware responses using Spring AI `ChatClient` with the user's recent meal log data injected as context
- Wire the existing `GET /api/v1/advice` endpoint to the frontend insights panel (no backend changes needed for insights)

## Capabilities

### New Capabilities

- `coach-chat`: Conversational AI chat for nutrition questions, with recent meal log data injected as context into each prompt
- `coach-insights`: Proactive insights panel displaying daily and weekly AI-generated advice fetched from the existing advice endpoints

### Modified Capabilities

<!-- No existing spec requirements are changing -->

## Impact

- **Backend**: New `CoachController` + `CoachService` in a new `coach` package; Spring AI `ChatClient` usage (already a dependency); reads `MealLogRepository` for context injection
- **Frontend**: New `Coach.tsx` page; new `NavLink` in `Layout.tsx`; two new API calls (`POST /api/v1/chat`, `GET /api/v1/advice`)
- **Dependencies**: No new dependencies — Spring AI and `claude-sonnet-4-6` model are already in use
