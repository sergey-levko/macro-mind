package com.epam.macromind.food;

import com.epam.macromind.AbstractIntegrationTest;
import com.epam.macromind.auth.AuthResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.CacheManager;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsdaGatingIntegrationTest extends AbstractIntegrationTest {

    static WireMockServer wireMock;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(0);
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("usda.api.base-url", wireMock::baseUrl);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        });
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private String register() {
        String body = """
                {
                  "name": "Gating User",
                  "email": "gating-%s@example.com",
                  "password": "password123",
                  "age": 30,
                  "weightKg": 70.0,
                  "heightCm": 175.0,
                  "goalType": "MAINTAIN_WEIGHT"
                }
                """.formatted(UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity(url("/api/v1/auth/register"),
                new HttpEntity<>(body, headers), AuthResponse.class).getBody().accessToken();
    }

    private HttpHeaders headersFor(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void disableUsda(String token) {
        restTemplate.exchange(url("/api/v1/settings"), HttpMethod.PUT,
                new HttpEntity<>("{\"usdaEnabled\":false}", headersFor(token)), Map.class);
    }

    private void enableUsda(String token) {
        restTemplate.exchange(url("/api/v1/settings"), HttpMethod.PUT,
                new HttpEntity<>("{\"usdaEnabled\":true}", headersFor(token)), Map.class);
    }

    @Test
    void usdaSearch_settingOff_returnsEmptyList() {
        String token = register();
        disableUsda(token);

        ResponseEntity<FoodResponse[]> response = restTemplate.exchange(
                url("/api/v1/foods/usda-search?q=chicken"), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), FoodResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        wireMock.verify(0, getRequestedFor(urlPathMatching("/fdc/v1/foods/search")));
    }

    @Test
    void usdaImport_settingOff_returns403() {
        String token = register();
        disableUsda(token);

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/foods/import"), HttpMethod.POST,
                new HttpEntity<>("{\"fdcId\": 12345}", headersFor(token)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().get("message")).asString().contains("disabled");
    }

    @Test
    @SuppressWarnings("unchecked")
    void usdaSearch_settingOn_returnsResults() {
        String token = register();

        String searchResponse = """
                {
                  "foods": [
                    {
                      "fdcId": 173944,
                      "description": "Chicken Breast",
                      "foodNutrients": [
                        {"nutrientId": 1008, "value": 165.0},
                        {"nutrientId": 1003, "value": 31.0},
                        {"nutrientId": 1005, "value": 0.0},
                        {"nutrientId": 1004, "value": 3.6}
                      ]
                    }
                  ]
                }
                """;
        wireMock.stubFor(get(urlPathMatching("/fdc/v1/foods/search"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(searchResponse)));

        ResponseEntity<UsdaFoodResult[]> response = restTemplate.exchange(
                url("/api/v1/foods/usda-search?q=chicken"), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), UsdaFoodResult[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void usdaImport_settingOn_succeeds() {
        String token = register();
        enableUsda(token);

        String usdaResponse = """
                {
                  "fdcId": 55555,
                  "description": "Turkey Breast",
                  "foodNutrients": [
                    {"nutrient": {"id": 1008}, "amount": 150.0},
                    {"nutrient": {"id": 1003}, "amount": 28.0},
                    {"nutrient": {"id": 1005}, "amount": 0.0},
                    {"nutrient": {"id": 1004}, "amount": 2.0}
                  ]
                }
                """;
        wireMock.stubFor(get(urlPathMatching("/fdc/v1/food/55555"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(usdaResponse)));

        ResponseEntity<FoodResponse> response = restTemplate.exchange(
                url("/api/v1/foods/import"), HttpMethod.POST,
                new HttpEntity<>("{\"fdcId\": 55555}", headersFor(token)), FoodResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().name()).isEqualTo("Turkey Breast");
    }
}
