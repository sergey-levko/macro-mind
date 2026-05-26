## ADDED Requirements

### Requirement: USDA food detail responses are cached server-side by FDC ID
The system SHALL maintain an in-process cache of USDA FoodData Central food detail responses, keyed by `fdcId`. A cached response SHALL be returned for subsequent requests for the same `fdcId` without making a new outbound HTTP call to the FDC API. The cache SHALL have a maximum size of 1 000 entries and SHALL evict entries 24 hours after they are written.

#### Scenario: Cache miss — first import of an FDC ID
- **WHEN** `POST /api/v1/foods/import` is called with an `fdcId` that has not been cached
- **THEN** the system makes one outbound HTTP request to the FDC detail endpoint, stores the response in the cache, and returns HTTP 201 with the imported food

#### Scenario: Cache hit — repeat import of the same FDC ID
- **WHEN** `POST /api/v1/foods/import` is called with an `fdcId` that is already in the cache
- **THEN** the system returns HTTP 201 with the imported food without making any outbound HTTP request to the FDC API

#### Scenario: Cache entry expires after TTL
- **WHEN** a cached entry for a given `fdcId` has been in the cache for 24 hours
- **THEN** the next import of that `fdcId` makes a fresh outbound HTTP request and re-populates the cache

#### Scenario: USDA search results are not cached
- **WHEN** `GET /api/v1/foods/usda-search` is called
- **THEN** the system always makes an outbound HTTP request to the FDC search endpoint; no caching is applied
