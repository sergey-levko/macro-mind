package com.epam.macromind.template;

import com.epam.macromind.AbstractIntegrationTest;
import com.epam.macromind.auth.AuthResponse;
import com.epam.macromind.food.CreateFoodRequest;
import com.epam.macromind.food.FoodResponse;
import com.epam.macromind.meal.MealLogResponse;
import com.epam.macromind.meal.MealLogSummaryResponse;
import com.epam.macromind.meal.MealItemResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MealTemplateIntegrationTest extends AbstractIntegrationTest {

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

    private String register() {
        String body = """
                {
                  "name": "Template User",
                  "email": "template-%s@example.com",
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
                new HttpEntity<>(body, headers), AuthResponse.class).getBody().accessToken();
    }

    private HttpHeaders headersFor(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private UUID createFood(String token) {
        CreateFoodRequest req = new CreateFoodRequest("Rice",
                new BigDecimal("130"), new BigDecimal("2.7"),
                new BigDecimal("28"), new BigDecimal("0.3"));
        return restTemplate.exchange(url("/api/v1/foods"), HttpMethod.POST,
                new HttpEntity<>(req, headersFor(token)), FoodResponse.class).getBody().id();
    }

    private UUID createMealWithItem(String token, String date, String mealType, UUID foodId, int qty) {
        String logBody = "{\"mealType\":\"" + mealType + "\",\"loggedAt\":\"" + date + "T00:00:00Z\"}";
        MealLogResponse log = restTemplate.exchange(url("/api/v1/meal-logs"), HttpMethod.POST,
                new HttpEntity<>(logBody, headersFor(token)), MealLogResponse.class).getBody();
        restTemplate.exchange(url("/api/v1/meal-logs/" + log.id() + "/items"), HttpMethod.POST,
                new HttpEntity<>("{\"foodId\":\"" + foodId + "\",\"quantityG\":" + qty + "}", headersFor(token)),
                MealItemResponse.class);
        return log.id();
    }

    @Test
    void saveTemplate_withMeals_returns201() {
        String token = register();
        UUID foodId = createFood(token);
        String today = LocalDate.now(ZoneOffset.UTC).toString();
        createMealWithItem(token, today, "BREAKFAST", foodId, 100);

        String body = "{\"name\":\"My Breakfast\",\"date\":\"" + today + "\"}";
        ResponseEntity<MealTemplateResponse> response = restTemplate.exchange(
                url("/api/v1/meal-templates"), HttpMethod.POST,
                new HttpEntity<>(body, headersFor(token)), MealTemplateResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().name()).isEqualTo("My Breakfast");
        assertThat(response.getBody().itemCount()).isEqualTo(1);
        assertThat(response.getBody().totals().caloriesKcal()).isEqualByComparingTo("130.00");
        assertThat(response.getBody().id()).isNotNull();
    }

    @Test
    void saveTemplate_noMealsOnDate_returns400() {
        String token = register();

        String body = "{\"name\":\"Empty Day\",\"date\":\"2099-01-01\"}";
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/meal-templates"), HttpMethod.POST,
                new HttpEntity<>(body, headersFor(token)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void listTemplates_returnsSavedTemplatesWithMacroTotals() {
        String token = register();
        UUID foodId = createFood(token);
        String today = LocalDate.now(ZoneOffset.UTC).toString();
        createMealWithItem(token, today, "LUNCH", foodId, 200);

        String saveBody = "{\"name\":\"My Lunch\",\"date\":\"" + today + "\"}";
        restTemplate.exchange(url("/api/v1/meal-templates"), HttpMethod.POST,
                new HttpEntity<>(saveBody, headersFor(token)), MealTemplateResponse.class);

        ResponseEntity<MealTemplateResponse[]> list = restTemplate.exchange(
                url("/api/v1/meal-templates"), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), MealTemplateResponse[].class);

        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(list.getBody()).hasSize(1);
        assertThat(list.getBody()[0].name()).isEqualTo("My Lunch");
        assertThat(list.getBody()[0].totals().caloriesKcal()).isEqualByComparingTo("260.00");
    }

    @Test
    void applyTemplate_createsMealLogsForDate() {
        String token = register();
        UUID foodId = createFood(token);
        String sourceDate = LocalDate.now(ZoneOffset.UTC).minusDays(3).toString();
        createMealWithItem(token, sourceDate, "DINNER", foodId, 150);

        String saveBody = "{\"name\":\"Dinner Template\",\"date\":\"" + sourceDate + "\"}";
        MealTemplateResponse template = restTemplate.exchange(
                url("/api/v1/meal-templates"), HttpMethod.POST,
                new HttpEntity<>(saveBody, headersFor(token)), MealTemplateResponse.class).getBody();

        String targetDate = LocalDate.now(ZoneOffset.UTC).minusDays(1).toString();
        String applyBody = "{\"date\":\"" + targetDate + "\"}";
        ResponseEntity<MealLogSummaryResponse[]> applied = restTemplate.exchange(
                url("/api/v1/meal-templates/" + template.id() + "/apply"), HttpMethod.POST,
                new HttpEntity<>(applyBody, headersFor(token)), MealLogSummaryResponse[].class);

        assertThat(applied.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(applied.getBody()).hasSize(1);

        ResponseEntity<MealLogSummaryResponse[]> logs = restTemplate.exchange(
                url("/api/v1/meal-logs?date=" + targetDate), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), MealLogSummaryResponse[].class);
        assertThat(logs.getBody()).hasSize(1);
        assertThat(logs.getBody()[0].totals().caloriesKcal()).isEqualByComparingTo("195.00");
    }

    @Test
    void applyTemplate_wrongUser_returns404() {
        String ownerToken = register();
        String otherToken = register();
        UUID foodId = createFood(ownerToken);
        String today = LocalDate.now(ZoneOffset.UTC).toString();
        createMealWithItem(ownerToken, today, "SNACK", foodId, 50);

        String saveBody = "{\"name\":\"Owner Template\",\"date\":\"" + today + "\"}";
        MealTemplateResponse template = restTemplate.exchange(
                url("/api/v1/meal-templates"), HttpMethod.POST,
                new HttpEntity<>(saveBody, headersFor(ownerToken)), MealTemplateResponse.class).getBody();

        String applyBody = "{\"date\":\"" + today + "\"}";
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/meal-templates/" + template.id() + "/apply"), HttpMethod.POST,
                new HttpEntity<>(applyBody, headersFor(otherToken)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteTemplate_returns204ThenListIsEmpty() {
        String token = register();
        UUID foodId = createFood(token);
        String today = LocalDate.now(ZoneOffset.UTC).toString();
        createMealWithItem(token, today, "BREAKFAST", foodId, 80);

        String saveBody = "{\"name\":\"To Delete\",\"date\":\"" + today + "\"}";
        MealTemplateResponse template = restTemplate.exchange(
                url("/api/v1/meal-templates"), HttpMethod.POST,
                new HttpEntity<>(saveBody, headersFor(token)), MealTemplateResponse.class).getBody();

        ResponseEntity<Void> deleted = restTemplate.exchange(
                url("/api/v1/meal-templates/" + template.id()), HttpMethod.DELETE,
                new HttpEntity<>(headersFor(token)), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<MealTemplateResponse[]> list = restTemplate.exchange(
                url("/api/v1/meal-templates"), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), MealTemplateResponse[].class);
        assertThat(list.getBody()).isEmpty();
    }
}
