## Context

The meal log food-add flow currently requires a search query for every food addition. The existing schema already contains all necessary data: `meal_items` records which foods were used, and `meal_logs` records when. No new tables or columns are needed — this feature is a read-only query over existing data plus a small frontend UI addition.

## Goals / Non-Goals

**Goals:**
- New `GET /api/v1/foods/recent` endpoint returning the authenticated user's N most recently used distinct foods
- Meal log food-add panel shows a "Recent" section when the search field is empty
- Section disappears when the user starts typing, replaced by search results

**Non-Goals:**
- Frequency/popularity ranking (recency only)
- Persistent favorites or user-managed pins
- Schema migrations — no new tables or columns
- Caching the recent-foods response (query is lightweight and must reflect the latest log)

## Decisions

### 1. Native SQL query via `@Query(nativeQuery = true)` on `FoodRepository`

The required query — distinct foods ordered by last usage time — is not expressible cleanly in JPQL. A native SQL subquery with `GROUP BY` and `ORDER BY last_used DESC LIMIT :limit` is straightforward and readable:

```sql
SELECT f.*
FROM foods f
JOIN (
    SELECT mi.food_id, MAX(ml.logged_at) AS last_used
    FROM meal_items mi
    JOIN meal_logs ml ON mi.meal_log_id = ml.id
    WHERE ml.user_id = :userId
    GROUP BY mi.food_id
    ORDER BY last_used DESC
    LIMIT :limit
) recent ON f.id = recent.food_id
ORDER BY recent.last_used DESC
```

Alternative considered: JPQL with a subquery. Rejected — JPQL aggregate + ORDER BY in this shape is brittle across Hibernate versions and harder to reason about.

### 2. Reuse `FoodResponse` DTO

The endpoint returns `List<FoodResponse>`, the same DTO already used by food search and import. No new DTO needed.

### 3. `limit` as a query parameter with a server-side cap of 10

The endpoint accepts `?limit=N` (default 10, max 10) to give the frontend flexibility without exposing unbounded queries. Frontend always passes the default.

### 4. Frontend: fetch on panel open, suppress on search input

Recent foods are fetched once when the food-add panel opens (or on page load) and stored in local state. The list is hidden as soon as `searchQuery.length > 0`. This avoids flickering: results appear immediately on panel open, then disappear the moment the user types.

## Risks / Trade-offs

- **Stale recent list after adding a food**: After a user adds a food via quick-add, the recent list won't update until the panel is next opened. Acceptable — the list refreshes on every panel open.
- **Foods owned by another user in meal_items**: Not possible — the query filters by `ml.user_id = :userId` and `foods.user_id` is also user-scoped by existing access control.
- **Empty state**: If the user has never logged a food, the recent section is simply not rendered. The search input is shown alone, same as today.

## Migration Plan

No database schema changes. Deploy is a standard backend + frontend release. No rollback concerns beyond reverting the PR.
