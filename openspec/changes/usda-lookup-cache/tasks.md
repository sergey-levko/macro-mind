## 1. Dependencies

- [x] 1.1 Add `spring-boot-starter-cache` and `com.github.ben-manes.caffeine:caffeine` to `backend/pom.xml`

## 2. Cache Configuration

- [x] 2.1 Add `@EnableCaching` to `MacroMindApplication.java`
- [x] 2.2 Add `spring.cache.type=caffeine` and `spring.cache.cache-names=usda-food` and `spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=24h` to `application.yml`

## 3. Cache the FDC Fetch Call

- [x] 3.1 Annotate `UsdaFoodClient.fetch(int fdcId)` with `@Cacheable("usda-food")`

## 4. Tests

- [x] 4.1 Add unit test to `UsdaFoodClientTest` (or new `UsdaFoodClientCacheTest`): verify that calling `fetch` twice with the same `fdcId` results in only one HTTP call (assert `RestClient` / WireMock stub called once)
- [x] 4.2 Update `FoodIntegrationTest`: clear the `usda-food` cache in `@BeforeEach` to prevent cache hits from polluting unrelated test methods
