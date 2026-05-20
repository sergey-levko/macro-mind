## 1. Domain Model

- [x] 1.1 Create `AdviceType` enum (`DAILY`, `WEEKLY`) in `com.epam.macromind.advice`
- [x] 1.2 Create `AiAdvice` JPA entity mapped to `ai_advice` table (id UUID, userId UUID, adviceType AdviceType, content TEXT, periodStart LocalDate, createdAt Instant)
- [x] 1.3 Create `AiAdviceRepository` (JpaRepository) with `findByUserIdAndAdviceTypeAndPeriodStart` and `findByUserId` query methods

## 2. DTOs and Exceptions

- [x] 2.1 Create `GenerateAdviceRequest` DTO with `@NotNull adviceType` and `@NotNull periodStart (LocalDate)`
- [x] 2.2 Create `AiAdviceResponse` DTO record (id, userId, adviceType, periodStart, content, createdAt)
- [x] 2.3 Create `AdviceNotFoundException` and `NoGoalForAdviceException` exception classes

## 3. Service and AI Integration

- [x] 3.1 Create `AdvicePromptBuilder` component: builds system prompt (user profile + nutritional goal) and user prompt (daily macro totals per day in period vs targets) from aggregated repository data
- [x] 3.2 Create `AiAdviceService` with `generateAdvice`, `getAdvice`, and `listAdvice` methods; inject `ChatClient`, `UserRepository`, `NutritionalGoalRepository`, `MealLogRepository`, `FoodRepository`, `AiAdviceRepository`

## 4. REST Controller and Exception Handler

- [x] 4.1 Create `AiAdviceController` (`POST /api/v1/advice` → 201, `GET /api/v1/advice/{id}` → 200, `GET /api/v1/advice` → 200 with optional `adviceType` and `periodStart` query params)
- [x] 4.2 Register `AdviceNotFoundException` → HTTP 404 and `NoGoalForAdviceException` → HTTP 400 in `GlobalExceptionHandler`

## 5. Tests

- [x] 5.1 Write `AiAdviceServiceTest` (Mockito): generate success, generate user not found, generate no goal, get advice, get advice not found, list advice (with and without filters)
- [x] 5.2 Write `AiAdviceControllerTest` (`@WebMvcTest`): POST 201, POST 400 missing fields, POST 404 unknown user, GET /{id} 200, GET /{id} 404, GET list 200
- [x] 5.3 Write `AiAdviceIntegrationTest` (Testcontainers + `@MockitoBean ChatClient`): generate → retrieve → list round-trip; 400 when no goal set
