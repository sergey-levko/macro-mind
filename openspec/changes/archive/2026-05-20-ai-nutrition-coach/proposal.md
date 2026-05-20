## Why

Users can now log meals, track macro totals, and set nutritional goals — but get no feedback on whether they're on track. The AI nutrition coach closes this loop by analyzing a user's recent intake against their goals and generating personalized, actionable advice powered by Claude AI. This is the primary value proposition of MacroMind.

## What Changes

- New endpoint to request an AI-generated nutrition advice report for a given period (daily or weekly)
- The system aggregates meal logs and compares them against the user's nutritional goal, then sends a structured prompt to Claude and returns the parsed advice
- Advice is persisted in the `ai_advice` table so users can review past reports without re-calling the API

## Capabilities

### New Capabilities
- `ai-advice`: Generate, retrieve, and list AI-powered nutrition advice. Supports on-demand advice generation (`POST`) for a given `adviceType` (DAILY/WEEKLY) and `periodStart` date, plus retrieval (`GET /{id}`) and listing (`GET ?adviceType=&periodStart=`) of stored advice.

### Modified Capabilities

## Impact

- **Database**: `ai_advice` table (id, user_id, advice_type ENUM, content TEXT, period_start DATE, created_at) — already in schema; no new Liquibase changeset needed
- **New API endpoints**: `POST /api/v1/advice`, `GET /api/v1/advice/{id}`, `GET /api/v1/advice?adviceType=&periodStart=`
- **New vertical slice**: `com.epam.macromind.advice` — entity, repository, service, controller, DTOs, Spring AI integration
- **Spring AI dependency**: already on classpath; requires `spring.ai.anthropic.api-key` configured
- **Cross-slice reads**: `MealLogRepository`, `NutritionalGoalRepository`, `UserRepository` used to build context for the prompt
- **GlobalExceptionHandler**: add handler for `AdviceNotFoundException`

## Non-goals

- Streaming AI responses (return full text when complete)
- Frontend implementation
- Automatic/scheduled advice generation
- Advice editing or user feedback on advice quality
