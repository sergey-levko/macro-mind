## Context

MacroMind already uses Spring AI with Claude for `ai-advice` generation. The `nutritional_goals` slice has a `NutritionalGoalService` and `NutritionalGoalController` with `GET` and `PUT` endpoints. The user's profile (age, weight, height, goal_type) is stored in the `users` table and is available to Claude as context. No schema changes are required.

## Goals / Non-Goals

**Goals:**
- New `POST /api/v1/nutritional-goals/generate` endpoint that returns a suggested `{ caloriesTarget, proteinG, carbsG, fatG }` without persisting it
- Claude prompt includes user profile and goal type; returns structured output parsed into a DTO
- Frontend "Generate with AI" button in the Dashboard goal form pre-fills fields with the suggestion; user must explicitly hit Save to persist

**Non-Goals:**
- Auto-saving the AI suggestion
- Explaining the reasoning behind the numbers in the UI (response contains only numbers)
- Adjusting suggestions based on meal history

## Decisions

**Return suggestion as a transient DTO, never auto-persist.**
- Rationale: User must retain control. Auto-saving a Claude response without confirmation would be unexpected behaviour.

**Use Spring AI `ChatClient` with structured output (`BeanOutputConverter`) into a `GoalSuggestion` record.**
- Rationale: Consistent with the existing `ai-advice` slice which uses the same pattern. Avoids manual JSON parsing.
- Alternative considered: Free-form text response parsed with regex — rejected as brittle.

**Prompt construction: include `goal_type`, `age`, `weight_kg`, `height_cm`; ask for TDEE-based recommendation.**
- Rationale: These four fields are the minimum needed to estimate calorie needs (Mifflin-St Jeor or similar). Protein/carbs/fat split is derived from goal type (e.g. GAIN_MUSCLE → higher protein ratio).

**New `GenerateGoalRequest` / `GoalSuggestionResponse` DTOs in the `goal` slice.**
- No new slice needed; extends the existing `nutritional-goals` vertical slice.
- Liquibase: No changeset required (no schema changes).

**Spring AI integration point:**
```
POST /api/v1/nutritional-goals/generate
  → NutritionalGoalController.generateGoal(userId)
  → NutritionalGoalService.generateGoal(userId)
      → UserRepository.findById(userId)  // fetch profile
      → ChatClient.prompt(built prompt).call().entity(GoalSuggestionResponse.class)
  ← GoalSuggestionResponse { caloriesTarget, proteinG, carbsG, fatG }
```

## Risks / Trade-offs

- [Claude API latency] Goal generation may take 1–3 seconds. → Show a loading spinner on the "Generate with AI" button; disable it while in flight.
- [Unexpected Claude output] Structured output parsing may fail if Claude returns non-numeric values. → Wrap in try/catch; return HTTP 502 with a clear error message so the frontend can show "Generation failed, please try again."
- [Cost] Each click calls Claude. → Acceptable at MVP scale; no caching needed yet.
