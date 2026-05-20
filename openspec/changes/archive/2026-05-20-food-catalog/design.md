## Context

The `foods` table is already defined in the DB schema with columns: `id` (UUID PK), `user_id` (FK → users), `name`, `source`, `calories_100g`, `protein_g`, `carbs_g`, `fat_g`. No schema migration is needed. The feature introduces a new `food` vertical slice following the same controller → service → repository → entity → DTO pattern established by the `user` slice.

USDA FoodData Central is the configured external data source (180,000+ foods). It is accessed via HTTP from `FoodService` using Spring's `RestClient`, with the API key injected from environment variable `USDA_API_KEY`.

## Goals / Non-Goals

**Goals:**
- Expose CRUD endpoints for user-owned custom foods
- Support name-based search scoped to the authenticated user's foods plus USDA-sourced foods already imported into the DB
- Integrate with USDA FoodData Central to import foods by `fdcId` into the local `foods` table

**Non-Goals:**
- Food update (PUT) endpoint
- Bulk USDA import
- Pagination for search results (first iteration returns up to 20 results)
- Authentication / authorization middleware (out of scope for this change)

## Decisions

### 1. No Liquibase changeset required
The `foods` table already exists in the DB schema from the initial scaffold. No DDL changes are needed.
*Alternative considered*: adding a `source` ENUM type (CUSTOM / USDA) — rejected to keep the schema simple; `source` remains a plain `VARCHAR`.

### 2. USDA integration via `RestClient` in `FoodService`
Spring Boot 3's `RestClient` (synchronous, fluent) is used to call `api.nal.usda.gov/fdc/v1/food/{fdcId}`. The response is mapped to an internal DTO and persisted as a `Food` entity with `source = "USDA"`.
*Alternative considered*: WebClient (reactive) — rejected because the rest of the stack is synchronous servlet-based.

### 3. Search scoped to DB only (no live USDA search)
`GET /api/v1/foods?search=` queries the local `foods` table (`ILIKE '%term%'` on `name`) filtered to the requesting user's own foods. Live USDA search is out of scope; users must import a USDA food by known `fdcId` first.
*Alternative considered*: proxying the USDA search endpoint — rejected to avoid latency and rate-limit concerns in this iteration.

### 4. `user_id` passed as request header
Since authentication is not yet implemented, `user_id` is passed as `X-User-Id` header (UUID). `FoodService` validates the user exists via `UserRepository` before creating or importing a food.

## Risks / Trade-offs

- **USDA API key not set** → `POST /api/v1/foods/import` returns 503 with a descriptive error; no silent failure.
- **Search returns stale USDA data** → imported USDA foods are snapshotted at import time; macro data may drift from USDA source. Mitigation: `source` field lets future tooling identify and refresh USDA-origin records.
- **No pagination on search** → capped at 20 results for now; acceptable for early-stage usage. A future change can add cursor-based pagination.
- **user_id header spoofing** → accepted risk until authentication is introduced; consistent with the approach used in the `user` slice.
