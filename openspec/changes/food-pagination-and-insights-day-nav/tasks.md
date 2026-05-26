## 1. Backend — Food Pagination

- [x] 1.1 Add `PageResponse<T>` wrapper DTO record (fields: `content`, `page`, `totalPages`, `totalElements`) in a shared `common` package
- [x] 1.2 Replace `findTop20By*` methods in `FoodRepository` with `Page<Food> findByUserIdOrderByNameAsc(UUID userId, Pageable pageable)` and `Page<Food> findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(UUID userId, String name, Pageable pageable)`
- [x] 1.3 Update `FoodService.searchFoods` to accept `int page, int size`, cap size at 50, use `PageRequest.of(page, size)`, and return `PageResponse<FoodResponse>`
- [x] 1.4 Update `FoodController.search` to accept `@RequestParam(defaultValue = "0") int page` and `@RequestParam(defaultValue = "20") int size`, return `PageResponse<FoodResponse>`
- [x] 1.5 Update `FoodServiceTest` unit tests for paginated service method
- [x] 1.6 Update `FoodControllerTest` / integration tests to assert paginated response shape (`content`, `page`, `totalPages`, `totalElements`) and page-size cap

## 2. Frontend — Food Pagination

- [x] 2.1 Update `Foods.tsx`: add `page` state (default 0), update `loadFoods` to pass `?page=<page>&size=20`, parse `PageResponse` envelope, reset to page 0 on search change
- [x] 2.2 Add pagination controls to `Foods.tsx`: "Previous" button (disabled on page 0), "Next" button (disabled on last page), and "Page X of Y" label; hide controls when `totalPages <= 1`

## 3. Frontend — Insights Day Navigation

- [x] 3.1 Update `Coach.tsx`: add `selectedDate` state (default `todayStr()`) to the Daily panel; remove `insightPeriod === 'history'` branch and all History-related state (`history`, `historyLoading`, `historyFilter`, `historyDate`, `deletingId`, `deleteInProgress`)
- [x] 3.2 Update the Daily insights load effect to use `selectedDate` instead of `todayStr()` — re-fetch on `selectedDate` change
- [x] 3.3 Add day-navigation controls to the Daily sub-tab: "Previous day" button, date label (`formatDateLabel(selectedDate)`), "Next day" button (disabled when `selectedDate === todayStr()`), and a date picker (reuse `HistoryDatePicker` logic, disable future dates)
- [x] 3.4 Update `generateInsight('DAILY')` and `saveInsight('DAILY')` to use `selectedDate` instead of `todayStr()`; clear preview when `selectedDate` changes
- [x] 3.5 Remove the "History" pill button and `insightPeriod` value `'history'` from the sub-tab switcher; update `insightPeriod` type to `'daily' | 'weekly'`
