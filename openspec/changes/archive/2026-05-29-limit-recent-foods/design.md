## Context

`FoodService.getRecentFoods` already applies `Math.min(limit, 10)` as a server-side guard. The existing spec documents the default as 5 but says nothing about a maximum, and the constant `10` is inline with no named identifier. The frontend always requests 10. The change is minimal: extract the constant, verify the spec reflects it, and add a targeted test if one is missing.

## Goals / Non-Goals

**Goals:**
- Extract the hard cap of 10 into a named constant `MAX_RECENT_FOODS`
- Update the `recent-foods` spec to document the hard maximum
- Add an integration test confirming requests above 10 are clamped

**Non-Goals:**
- Changing the default limit value
- Exposing the cap as a configurable property
- Modifying the query or database schema

## Decisions

**Extract constant vs. leave inline**
Extract `MAX_RECENT_FOODS = 10` in `FoodService`. Single source of truth; the same value used in the test assertion and the spec commentary. Inline magic numbers are easy to miss when the cap needs adjusting later.

**No API change**
The clamping is silent (no error for `limit > 10`, just returns 10 results). This is already the behaviour in production and matches the UX goal of "never show more than 10 recent foods". Returning an error for an oversized limit would be a breaking change with no benefit.

**No frontend change**
`MealLog.tsx` already passes `?limit=10`, which equals the cap exactly. No behavioural change.

## Risks / Trade-offs

- [Risk] Tests may already cover the 10-item cap via the existing `search_pageSizeCap` test → Mitigation: check `FoodIntegrationTest` for a recent-foods cap test; add one only if absent.

## Migration Plan

No database migration required. Backend-only change — deploy normally.
