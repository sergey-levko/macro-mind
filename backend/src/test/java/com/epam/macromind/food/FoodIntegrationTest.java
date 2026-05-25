package com.epam.macromind.food;

import com.epam.macromind.auth.AuthResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class FoodIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

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

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private String register() {
        String body = """
                {
                  "name": "Test User",
                  "email": "food-test-%s@example.com",
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
                new HttpEntity<>(body, headers), AuthResponse.class).getBody().token();
    }

    private HttpHeaders headersFor(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void createAndRetrieve_fullRoundTrip() {
        String token = register();
        CreateFoodRequest req = new CreateFoodRequest("Brown Rice",
                new BigDecimal("370"), new BigDecimal("7.9"),
                new BigDecimal("77"), new BigDecimal("2.9"));

        ResponseEntity<FoodResponse> created = restTemplate.exchange(
                url("/api/v1/foods"), HttpMethod.POST,
                new HttpEntity<>(req, headersFor(token)), FoodResponse.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody().source()).isEqualTo("CUSTOM");
        UUID foodId = created.getBody().id();

        ResponseEntity<FoodResponse> fetched = restTemplate.getForEntity(
                url("/api/v1/foods/" + foodId), FoodResponse.class);

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody().name()).isEqualTo("Brown Rice");
    }

    @Test
    void search_byName_returnsMatch() {
        String token = register();
        CreateFoodRequest req = new CreateFoodRequest("Oat Porridge",
                new BigDecimal("68"), new BigDecimal("2.4"),
                new BigDecimal("12"), new BigDecimal("1.4"));
        restTemplate.exchange(url("/api/v1/foods"), HttpMethod.POST,
                new HttpEntity<>(req, headersFor(token)), FoodResponse.class);

        ResponseEntity<FoodResponse[]> results = restTemplate.exchange(
                url("/api/v1/foods?search=oat"), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), FoodResponse[].class);

        assertThat(results.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(results.getBody()).anyMatch(f -> f.name().equals("Oat Porridge"));
    }

    @Test
    void delete_removesFood() {
        String token = register();
        CreateFoodRequest req = new CreateFoodRequest("Temp Food",
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
        ResponseEntity<FoodResponse> created = restTemplate.exchange(
                url("/api/v1/foods"), HttpMethod.POST,
                new HttpEntity<>(req, headersFor(token)), FoodResponse.class);
        UUID foodId = created.getBody().id();

        ResponseEntity<Void> deleted = restTemplate.exchange(
                url("/api/v1/foods/" + foodId), HttpMethod.DELETE,
                new HttpEntity<>(headersFor(token)), Void.class);

        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Map> fetched = restTemplate.getForEntity(url("/api/v1/foods/" + foodId), Map.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void delete_anotherUsersFood_returns403() {
        String ownerToken = register();
        String otherToken = register();

        CreateFoodRequest req = new CreateFoodRequest("Owner Food",
                BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
        ResponseEntity<FoodResponse> created = restTemplate.exchange(
                url("/api/v1/foods"), HttpMethod.POST,
                new HttpEntity<>(req, headersFor(ownerToken)), FoodResponse.class);
        UUID foodId = created.getBody().id();

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/foods/" + foodId), HttpMethod.DELETE,
                new HttpEntity<>(headersFor(otherToken)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void importFood_fromUsda_persistsWithUsdaSource() {
        String token = register();
        String usdaResponse = """
                {
                  "fdcId": 12345,
                  "description": "Chicken Breast",
                  "foodNutrients": [
                    {"nutrient": {"id": 1008}, "amount": 165.0},
                    {"nutrient": {"id": 1003}, "amount": 31.0},
                    {"nutrient": {"id": 1005}, "amount": 0.0},
                    {"nutrient": {"id": 1004}, "amount": 3.6}
                  ]
                }
                """;

        wireMock.stubFor(get(urlPathMatching("/fdc/v1/food/12345"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(usdaResponse)));

        ResponseEntity<FoodResponse> imported = restTemplate.exchange(
                url("/api/v1/foods/import"), HttpMethod.POST,
                new HttpEntity<>("{\"fdcId\": 12345}", headersFor(token)), FoodResponse.class);

        assertThat(imported.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(imported.getBody().source()).isEqualTo("USDA");
        assertThat(imported.getBody().name()).isEqualTo("Chicken Breast");
    }
}
