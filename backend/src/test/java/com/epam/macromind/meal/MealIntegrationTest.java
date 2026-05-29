package com.epam.macromind.meal;

import com.epam.macromind.AbstractIntegrationTest;
import com.epam.macromind.auth.AuthResponse;
import com.epam.macromind.food.CreateFoodRequest;
import com.epam.macromind.food.FoodResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MealIntegrationTest extends AbstractIntegrationTest {

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("usda.api.base-url", () -> "http://localhost:9999");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private AuthResponse register() {
        String body = """
                {
                  "name": "Meal User",
                  "email": "meal-%s@example.com",
                  "password": "password123",
                  "age": 28,
                  "weightKg": 70.0,
                  "heightCm": 175.0,
                  "goalType": "MAINTAIN_WEIGHT"
                }
                """.formatted(UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity(url("/api/v1/auth/register"),
                new HttpEntity<>(body, headers), AuthResponse.class).getBody();
    }

    private UUID createFood(String token) {
        CreateFoodRequest req = new CreateFoodRequest("Chicken",
                new BigDecimal("165"), new BigDecimal("31"),
                BigDecimal.ZERO, new BigDecimal("3.6"));
        return restTemplate.exchange(url("/api/v1/foods"), HttpMethod.POST,
                new HttpEntity<>(req, headersFor(token)), FoodResponse.class).getBody().id();
    }

    private HttpHeaders headersFor(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void createAndRetrieve_fullRoundTrip() {
        String token = register().accessToken();

        ResponseEntity<MealLogResponse> created = restTemplate.exchange(
                url("/api/v1/meal-logs"), HttpMethod.POST,
                new HttpEntity<>("{\"mealType\":\"BREAKFAST\"}", headersFor(token)),
                MealLogResponse.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody().mealType()).isEqualTo(MealType.BREAKFAST);
        UUID logId = created.getBody().id();

        ResponseEntity<MealLogResponse> fetched = restTemplate.exchange(
                url("/api/v1/meal-logs/" + logId), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), MealLogResponse.class);

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody().id()).isEqualTo(logId);
        assertThat(fetched.getBody().items()).isEmpty();
    }

    @Test
    void addItem_computesCorrectMacros() {
        String token = register().accessToken();
        UUID foodId = createFood(token);

        ResponseEntity<MealLogResponse> log = restTemplate.exchange(
                url("/api/v1/meal-logs"), HttpMethod.POST,
                new HttpEntity<>("{\"mealType\":\"LUNCH\"}", headersFor(token)),
                MealLogResponse.class);
        UUID logId = log.getBody().id();

        String itemBody = "{\"foodId\":\"" + foodId + "\",\"quantityG\":200}";
        ResponseEntity<MealItemResponse> item = restTemplate.exchange(
                url("/api/v1/meal-logs/" + logId + "/items"), HttpMethod.POST,
                new HttpEntity<>(itemBody, headersFor(token)), MealItemResponse.class);

        assertThat(item.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(item.getBody().calories()).isEqualByComparingTo("330.00");
        assertThat(item.getBody().proteinG()).isEqualByComparingTo("62.00");
    }

    @Test
    void listByDate_returnsOnlyThatDaysLogs() {
        String token = register().accessToken();
        restTemplate.exchange(url("/api/v1/meal-logs"), HttpMethod.POST,
                new HttpEntity<>("{\"mealType\":\"DINNER\"}", headersFor(token)),
                MealLogResponse.class);

        ResponseEntity<MealLogSummaryResponse[]> results = restTemplate.exchange(
                url("/api/v1/meal-logs?date=2099-01-01"), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), MealLogSummaryResponse[].class);

        assertThat(results.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(results.getBody()).isEmpty();
    }

    @Test
    void delete_removesLogAndItems() {
        String token = register().accessToken();
        UUID foodId = createFood(token);

        ResponseEntity<MealLogResponse> log = restTemplate.exchange(
                url("/api/v1/meal-logs"), HttpMethod.POST,
                new HttpEntity<>("{\"mealType\":\"SNACK\"}", headersFor(token)),
                MealLogResponse.class);
        UUID logId = log.getBody().id();

        restTemplate.exchange(url("/api/v1/meal-logs/" + logId + "/items"), HttpMethod.POST,
                new HttpEntity<>("{\"foodId\":\"" + foodId + "\",\"quantityG\":100}", headersFor(token)),
                MealItemResponse.class);

        ResponseEntity<Void> deleted = restTemplate.exchange(
                url("/api/v1/meal-logs/" + logId), HttpMethod.DELETE,
                new HttpEntity<>(headersFor(token)), Void.class);

        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Map> fetched = restTemplate.exchange(
                url("/api/v1/meal-logs/" + logId), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), Map.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addItem_toAnotherUsersLog_returns403() {
        String ownerToken = register().accessToken();
        String otherToken = register().accessToken();

        ResponseEntity<MealLogResponse> log = restTemplate.exchange(
                url("/api/v1/meal-logs"), HttpMethod.POST,
                new HttpEntity<>("{\"mealType\":\"BREAKFAST\"}", headersFor(ownerToken)),
                MealLogResponse.class);
        UUID logId = log.getBody().id();

        UUID foodId = createFood(ownerToken);
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/meal-logs/" + logId + "/items"), HttpMethod.POST,
                new HttpEntity<>("{\"foodId\":\"" + foodId + "\",\"quantityG\":100}",
                        headersFor(otherToken)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
