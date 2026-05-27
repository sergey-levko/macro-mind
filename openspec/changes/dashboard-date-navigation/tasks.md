## 1. Frontend — Dashboard date navigator

- [x] 1.1 In `Dashboard.tsx`, add `selectedDate` state (initialised to `todayStr()`), a `weekStart` derived value (Monday of `selectedDate`'s week), and helper functions `todayStr()` / `mondayOfDate(dateStr)` matching the pattern in `MealLog.tsx`
- [x] 1.2 Replace the hardcoded `today` in both `useEffect` fetch calls with `selectedDate` (daily summary) and the derived `weekStart` (weekly chart), so both re-fetch when `selectedDate` changes
- [x] 1.3 Render the date navigator row above the summary card: Previous button, formatted date label (e.g. "Today, May 26" or "May 25"), Next button (disabled when `selectedDate === todayStr()`), and a "Today" shortcut button (hidden when already on today)
