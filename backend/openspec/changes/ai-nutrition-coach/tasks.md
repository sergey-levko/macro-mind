## 1. Backend — Chat Endpoint

- [x] 1.1 Create `ChatRequest` record in `com.epam.macromind.coach` with `@NotBlank String message`
- [x] 1.2 Create `ChatResponse` record with `String reply`
- [x] 1.3 Create `CoachService` that injects `ChatClient` and `MealLogRepository`; implement `chat(UUID userId, String message)` — fetches last 7 days of meal logs, builds context string, calls ChatClient, returns reply
- [x] 1.4 Create `CoachController` with `POST /api/v1/chat`, reads `X-User-Id` header, delegates to `CoachService`

## 2. Frontend — Coach Page

- [x] 2.1 Create `frontend/src/pages/Coach.tsx` with a two-panel layout: chat panel (left/top) and insights panel (right/bottom)
- [x] 2.2 Implement chat panel: message input, send button, conversation list showing user messages and AI replies, loading state while request is in-flight
- [x] 2.3 Implement insights panel: fetch `GET /api/v1/advice` for `DAILY` and `WEEKLY` types on mount, display each insight, handle empty and error states

## 3. Frontend — Navigation

- [x] 3.1 Add `import Coach from './pages/Coach'` and `<Route path="coach" element={<Coach />} />` to `App.tsx`
- [x] 3.2 Add Coach `NavLink` to `Layout.tsx` sidebar (between Meal Log and Foods)
