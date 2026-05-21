## 1. DTOs

- [x] 1.1 Create `MacroTotals` record (caloriesKcal, proteinG, carbsG, fatG — all BigDecimal)
- [x] 1.2 Create `MacroTargets` record (caloriesTarget, proteinG, carbsG, fatG — all BigDecimal, nullable)
- [x] 1.3 Create `DailyDashboardResponse` record (date LocalDate, totals MacroTotals, targets MacroTargets)
- [x] 1.4 Create `DailyEntry` record (date LocalDate, totals MacroTotals) and `WeeklyDashboardResponse` record (weekStart LocalDate, days List<DailyEntry>, weeklyTotals MacroTotals, weeklyTargets MacroTargets)
- [x] 1.5 Create `MacroPercentages` record (caloriesPct, proteinPct, carbsPct, fatPct — all Integer, capped at 200) and `SummaryDashboardResponse` record (date LocalDate, totals MacroTotals, targets MacroTargets, percentages MacroPercentages)

## 2. Service

- [x] 2.1 Create `DashboardService` with `getDailySummary(UUID userId, LocalDate date)`: aggregates meal items for the day, computes macro totals, fetches goal targets (null if absent), returns `DailyDashboardResponse`
- [x] 2.2 Add `getWeeklySummary(UUID userId, LocalDate weekStart)` to `DashboardService`: iterates 7 days, reuses daily aggregation logic, computes weekly totals and targets × 7
- [x] 2.3 Add `getSummaryCard(UUID userId)` to `DashboardService`: calls daily aggregation for today (UTC), computes percentage per macro (capped at 200), returns `SummaryDashboardResponse`

## 3. Controller

- [x] 3.1 Create `DashboardController` (`GET /api/v1/dashboard/daily`, `GET /api/v1/dashboard/weekly`, `GET /api/v1/dashboard/summary`) — all return 200, all read `X-User-Id` header; `daily` requires `date` param, `weekly` requires `weekStart` param

## 4. Tests

- [ ] 4.1 Write `DashboardServiceTest` (Mockito): daily with meals, daily no meals, daily no goal, weekly with partial data, weekly no goal, summary card with goal, summary card no goal
- [ ] 4.2 Write `DashboardControllerTest` (`@WebMvcTest`): GET daily 200, GET daily 400 missing param, GET weekly 200, GET weekly 400 missing param, GET summary 200
- [ ] 4.3 Write `DashboardIntegrationTest` (Testcontainers): create user + goal + meal logs → assert daily/weekly/summary return correct aggregated totals; assert summary with no goal returns null targets
