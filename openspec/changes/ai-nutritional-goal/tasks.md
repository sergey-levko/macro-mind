## 1. Backend — DTOs and Service Method

- [ ] 1.1 Add `GoalSuggestionResponse` record (`caloriesTarget`, `proteinG`, `carbsG`, `fatG`) to the `goal` slice; add `generateGoal(UUID userId)` method to `NutritionalGoalService` that fetches the user profile, builds the Claude prompt, calls `ChatClient` with `BeanOutputConverter<GoalSuggestionResponse>`, and returns the result; wrap the call in try/catch and throw a `GoalGenerationException` (mapped to HTTP 502) on parse failure

## 2. Backend — Controller Endpoint

- [ ] 2.1 Add `POST /api/v1/nutritional-goals/generate` endpoint to `NutritionalGoalController` that delegates to `NutritionalGoalService.generateGoal(userId)` and returns 200 with the `GoalSuggestionResponse`; add `GoalGenerationException` handler returning HTTP 502

## 3. Backend — Tests

- [ ] 3.1 Add unit tests for `NutritionalGoalService.generateGoal()`: verify prompt construction, successful response mapping, and `GoalGenerationException` on parse failure (Mockito mock for `ChatClient`)
- [ ] 3.2 Add integration test for `POST /api/v1/nutritional-goals/generate` using Testcontainers and a mocked Spring AI `ChatClient` bean; assert 200 with numeric fields and 404 for unknown user

## 4. Frontend — Generate with AI Button

- [ ] 4.1 Add a "Generate with AI" button to the `GoalForm` in `Dashboard.tsx`; on click call `POST /api/v1/nutritional-goals/generate`, pre-fill the four form fields with the response values, show a loading spinner while in flight, disable the button during the request, and display "Generation failed, please try again" on error
