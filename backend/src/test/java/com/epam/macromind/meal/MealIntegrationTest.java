package com.epam.macromind.meal;

import com.epam.macromind.food.CreateFoodRequest;
import com.epam.macromind.food.FoodResponse;
import com.epam.macromind.user.CreateUserRequest;
import com.epam.macromind.user.GoalType;
import com.epam.macromind.user.UserResponse;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class MealIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

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

    private UUID createUser() {
        CreateUserRequest req = new CreateUserRequest(
                "Meal User", "meal-" + UUID.randomUUID() + "@example.com",
                28, new BigDecimal("70.0"), new BigDecimal("175.0"), GoalType.MAINTAIN_WEIGHT);
        return restTemplate.postForEntity(url("/api/v1/users"), req, UserResponse.class)
                .getBody().id();
    }

    private UUID createFood(UUID userId) {
        CreateFoodRequest req = new CreateFoodRequest("Chicken",
                new BigDecimal("165"), new BigDecimal("31"),
                BigDecimal.ZERO, new BigDecimal("3.6"));
        HttpHeaders headers = headersFor(userId);
        return restTemplate.exchange(url("/api/v1/foods"), HttpMethod.POST,
                new HttpEntity<>(req, headers), FoodResponse.class).getBody().id();
    }

    private HttpHeaders headersFor(UUID userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void createAndRetrieve_fullRoundTrip() {
        UUID userId = createUser();

        ResponseEntity<MealLogResponse> created = restTemplate.exchange(
                url("/api/v1/meal-logs"), HttpMethod.POST,
                new HttpEntity<>("{\"mealType\":\"BREAKFAST\"}", headersFor(userId)),
                MealLogResponse.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody().mealType()).isEqualTo(MealType.BREAKFAST);
        UUID logId = created.getBody().id();

        ResponseEntity<MealLogResponse> fetched = restTemplate.getForEntity(
                url("/api/v1/meal-logs/" + logId), MealLogResponse.class);

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody().id()).isEqualTo(logId);
        assertThat(fetched.getBody().items()).isEmpty();
    }

    @Test
    void addItem_computesCorrectMacros() {
        UUID userId = createUser();
        UUID foodId = createFood(userId);

        ResponseEntity<MealLogResponse> log = restTemplate.exchange(
                url("/api/v1/meal-logs"), HttpMethod.POST,
                new HttpEntity<>("{\"mealType\":\"LUNCH\"}", headersFor(userId)),
                MealLogResponse.class);
        UUID logId = log.getBody().id();

        String itemBody = "{\"foodId\":\"" + foodId + "\",\"quantityG\":200}";
        ResponseEntity<MealItemResponse> item = restTemplate.exchange(
                url("/api/v1/meal-logs/" + logId + "/items"), HttpMethod.POST,
                new HttpEntity<>(itemBody, headersFor(userId)), MealItemResponse.class);

        assertThat(item.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(item.getBody().calories()).isEqualByComparingTo("330.00");
        assertThat(item.getBody().proteinG()).isEqualByComparingTo("62.00");
    }

    @Test
    void listByDate_returnsOnlyThatDaysLogs() {
        UUID userId = createUser();
        restTemplate.exchange(url("/api/v1/meal-logs"), HttpMethod.POST,
                new HttpEntity<>("{\"mealType\":\"DINNER\"}", headersFor(userId)),
                MealLogResponse.class);

        ResponseEntity<MealLogSummaryResponse[]> results = restTemplate.exchange(
                url("/api/v1/meal-logs?date=2099-01-01"), HttpMethod.GET,
                new HttpEntity<>(headersFor(userId)), MealLogSummaryResponse[].class);

        assertThat(results.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(results.getBody()).isEmpty();
    }

    @Test
    void delete_removesLogAndItems() {
        UUID userId = createUser();
        UUID foodId = createFood(userId);

        ResponseEntity<MealLogResponse> log = restTemplate.exchange(
                url("/api/v1/meal-logs"), HttpMethod.POST,
                new HttpEntity<>("{\"mealType\":\"SNACK\"}", headersFor(userId)),
                MealLogResponse.class);
        UUID logId = log.getBody().id();

        restTemplate.exchange(url("/api/v1/meal-logs/" + logId + "/items"), HttpMethod.POST,
                new HttpEntity<>("{\"foodId\":\"" + foodId + "\",\"quantityG\":100}", headersFor(userId)),
                MealItemResponse.class);

        ResponseEntity<Void> deleted = restTemplate.exchange(
                url("/api/v1/meal-logs/" + logId), HttpMethod.DELETE,
                new HttpEntity<>(headersFor(userId)), Void.class);

        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Map> fetched = restTemplate.getForEntity(
                url("/api/v1/meal-logs/" + logId), Map.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addItem_toAnotherUsersLog_returns403() {
        UUID ownerUserId = createUser();
        UUID otherUserId = createUser();

        ResponseEntity<MealLogResponse> log = restTemplate.exchange(
                url("/api/v1/meal-logs"), HttpMethod.POST,
                new HttpEntity<>("{\"mealType\":\"BREAKFAST\"}", headersFor(ownerUserId)),
                MealLogResponse.class);
        UUID logId = log.getBody().id();

        UUID foodId = createFood(ownerUserId);
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/meal-logs/" + logId + "/items"), HttpMethod.POST,
                new HttpEntity<>("{\"foodId\":\"" + foodId + "\",\"quantityG\":100}",
                        headersFor(otherUserId)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
