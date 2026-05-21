## 1. Frontend — Date Navigation State

- [x] 1.1 Replace `todayIso()` constant in `MealLog.tsx` with `selectedDate` state (defaulting to today); add prev/next day buttons and a readable date label to the page header; disable the next button when `selectedDate === todayIso()`
- [x] 1.2 Update the `GET /api/v1/meal-logs` fetch to pass `selectedDate`; update `POST /api/v1/meal-logs` calls to set `loggedAt` to midnight UTC of `selectedDate`; re-fetch meals whenever `selectedDate` changes
