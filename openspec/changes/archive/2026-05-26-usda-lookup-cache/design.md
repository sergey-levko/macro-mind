## Context

`UsdaFoodClient.fetch(int fdcId)` is called on every `POST /api/v1/foods/import` request. Because FDC food records are static (nutrient data for a given `fdcId` never changes), the same remote call can be served from a local cache on subsequent requests. Currently there is no caching anywhere in the backend. The USDA `DEMO_KEY` is rate-limited to 40 requests per hour per IP; exceeding it returns HTTP 429 and the app surfaces a `UsdaServiceUnavailableException`.

`UsdaFoodClient.search(String query)` is not cached — search results depend on a free-text query string and are less predictable, so caching them is out of scope.

## Goals / Non-Goals

**Goals:**
- Cache `UsdaFoodClient.fetch(fdcId)` responses in-process using Caffeine
- Reduce import latency for repeated FDC IDs (cache hit returns immediately)
- Reduce USDA API quota consumption

**Non-Goals:**
- Caching USDA search results
- Distributed / Redis cache (single-process app, no need)
- Cache invalidation UI or admin endpoint
- Persisting cache across restarts

## Decisions

### Decision 1 — Spring Cache abstraction + Caffeine over manual HashMap

**Chosen:** `@EnableCaching` + `@Cacheable("usda-food")` on `UsdaFoodClient.fetch`, backed by Caffeine.

**Why:** Spring Cache is already on the classpath via `spring-boot-starter-cache`. `@Cacheable` integrates cleanly with Spring's proxy model — no manual cache code in the service layer. Caffeine is the recommended high-performance in-process cache for Spring Boot 3; it provides TTL, max-size, and statistics out of the box.

**Alternative considered:** Manual `ConcurrentHashMap<Integer, UsdaFoodDto>` in `UsdaFoodClient`. Rejected: no TTL, no size bound, no statistics, and it bypasses Spring's lifecycle management.

### Decision 2 — Cache scope: `fetch` only (not `search`)

**Chosen:** Cache only `UsdaFoodClient.fetch(int fdcId)`.

**Why:** FDC food detail records (`/fdc/v1/food/{fdcId}`) are immutable — nutrient data for a given ID never changes. Caching is safe indefinitely (capped only by TTL for memory hygiene). Search results vary by query string, change as the USDA database grows, and are rarely repeated character-for-character. The benefit-to-complexity ratio for search caching is low.

### Decision 3 — TTL 24 h, max 1 000 entries

**Chosen:** `maximumSize=1000,expireAfterWrite=24h` configured via `spring.cache.caffeine.spec`.

**Why:** 1 000 entries covers the realistic variety of foods a single-user instance would import. 24 h TTL is long enough to eliminate duplicate calls within any session while ensuring cache entries are not held indefinitely. USDA records are static so a longer TTL would also be correct, but 24 h is a reasonable hygiene bound.

**Alternative considered:** No TTL (entries live until eviction). Rejected: in a long-running process, stale `UsdaFoodDto` objects could accumulate and hold heap unnecessarily.

### Decision 4 — `@EnableCaching` on main application class

**Chosen:** Add `@EnableCaching` to `MacroMindApplication.java`.

**Why:** Centralised in the single application entrypoint; no new configuration class required. Keeps the change minimal.

## Risks / Trade-offs

- **Cache miss on first import still calls FDC** → Expected behaviour; the improvement is on repeated imports of the same fdcId.
- **In-process cache is lost on restart** → Acceptable; food data is stable and the quota cost of re-fetching after a restart is negligible.
- **`@Cacheable` requires Spring proxy** → `UsdaFoodClient` is a `@Component`; calling `fetch` through the Spring-managed bean (which `FoodService` already does via constructor injection) will use the proxy correctly. Self-invocation within the class would bypass it, but there is no self-invocation here.
- **Test isolation** — existing `FoodIntegrationTest` uses WireMock; `@Cacheable` can cause test pollution across test methods if the cache is not cleared between them. Mitigate by adding `@DirtiesContext` or a `CacheManager.clearAll()` `@BeforeEach` in relevant tests.
