## Why

Every call to `POST /api/v1/foods/import` triggers a live HTTP request to the USDA FoodData Central API — even when the same `fdcId` has been fetched before. This inflates response latency, consumes the shared USDA API quota (DEMO_KEY is rate-limited to 40 req/hour per IP), and degrades reliability when the FDC API is slow or unavailable. A simple in-memory cache on the backend eliminates repeat round-trips for identical FDC IDs.

## What Changes

- Add Spring Cache + Caffeine in-memory cache to `UsdaFoodClient.fetch(fdcId)` so repeated imports of the same food ID are served from cache
- Add `spring-boot-starter-cache` and `com.github.ben-manes.caffeine:caffeine` to `pom.xml`
- Enable caching via `@EnableCaching` on the application class
- Configure the `usda-food` cache (TTL, max size) in `application.yml`

## Capabilities

### New Capabilities

- `food-lookup-cache`: Server-side caching of USDA FoodData Central detail responses by `fdcId`, reducing external API calls and improving import latency.

### Modified Capabilities

*(none — the existing food-catalog API contract is unchanged; caching is transparent to clients)*

## Impact

- **`food/UsdaFoodClient.java`** — add `@Cacheable("usda-food")` to `fetch(int fdcId)`
- **`MacroMindApplication.java`** — add `@EnableCaching`
- **`pom.xml`** — new dependencies: `spring-boot-starter-cache`, `caffeine`
- **`application.yml`** — new `spring.cache.caffeine.spec` config (TTL 24 h, max 1 000 entries)
- No database tables affected, no Liquibase changesets needed
- No API endpoints added or modified
