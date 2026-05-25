package com.epam.macromind.dashboard;

import com.epam.macromind.auth.AuthResponse;
import com.epam.macromind.food.CreateFoodRequest;
import com.epam.macromind.food.FoodResponse;
import com.epam.macromind.meal.MealLogResponse;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class DashboardIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("usda.api.base-url", () -> "http://localhost:9999");
    }

    @LocalServerPort int port;
    @Autowired TestRestTemplate restTemplate;

    private String url(String path) { return "http://localhost:" + port + path; }

    private HttpHeaders headersFor(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private String register() {
        String body = """
                {
                  "name": "Dash User",
                  "email": "dash-%s@test.com",
                  "password": "password123",
                  "age": 30,
                  "weightKg": 75.0,
                  "heightCm": 180.0,
                  "goalType": "MAINTAIN_WEIGHT"
                }
                """.formatted(UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity(url("/api/v1/auth/register"),
                new HttpEntity<>(body, headers), AuthResponse.class).getBody().token();
    }

    private void setGoal(String token) {
        String body = "{\"caloriesTarget\":2000,\"proteinG\":150,\"carbsG\":200,\"fatG\":70}";
        restTemplate.exchange(url("/api/v1/nutritional-goals"), HttpMethod.PUT,
                new HttpEntity<>(body, headersFor(token)), Map.class);
    }

    private UUID createFood(String token) {
        var req = new CreateFoodRequest("Rice", new BigDecimal("130"),
                new BigDecimal("3"), new BigDecimal("28"), new BigDecimal("0.3"));
        return restTemplate.exchange(url("/api/v1/foods"), HttpMethod.POST,
                new HttpEntity<>(req, headersFor(token)), FoodResponse.class).getBody().id();
    }

    private void createMealLog(String token, UUID foodId, String date) {
        String body = String.format(
                "{\"mealType\":\"LUNCH\",\"loggedAt\":\"%sT12:00:00Z\",\"items\":[{\"foodId\":\"%s\",\"quantityG\":200}]}",
                date, foodId);
        restTemplate.exchange(url("/api/v1/meal-logs"), HttpMethod.POST,
                new HttpEntity<>(body, headersFor(token)), MealLogResponse.class);
    }

    @Test
    void dailyWeeklySummary_correctAggregatedTotals() {
        String token = register();
        setGoal(token);
        UUID foodId = createFood(token);
        String today = LocalDate.now().toString();
        createMealLog(token, foodId, today);

        ResponseEntity<DailyDashboardResponse> daily = restTemplate.exchange(
                url("/api/v1/dashboard/daily?date=" + today), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), DailyDashboardResponse.class);
        assertThat(daily.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(daily.getBody().totals().caloriesKcal()).isEqualByComparingTo("260.00");
        assertThat(daily.getBody().targets().caloriesTarget()).isEqualByComparingTo("2000");

        String weekStart = LocalDate.now().with(DayOfWeek.MONDAY).toString();
        ResponseEntity<WeeklyDashboardResponse> weekly = restTemplate.exchange(
                url("/api/v1/dashboard/weekly?weekStart=" + weekStart), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), WeeklyDashboardResponse.class);
        assertThat(weekly.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(weekly.getBody().days()).hasSize(7);
        assertThat(weekly.getBody().weeklyTargets().caloriesTarget()).isEqualByComparingTo("14000");

        ResponseEntity<SummaryDashboardResponse> summary = restTemplate.exchange(
                url("/api/v1/dashboard/summary"), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), SummaryDashboardResponse.class);
        assertThat(summary.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(summary.getBody().percentages()).isNotNull();
        assertThat(summary.getBody().percentages().caloriesPct()).isEqualTo(13);
    }

    @Test
    void summary_noGoal_returnsNullTargetsAndPercentages() {
        String token = register();

        ResponseEntity<SummaryDashboardResponse> summary = restTemplate.exchange(
                url("/api/v1/dashboard/summary"), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), SummaryDashboardResponse.class);

        assertThat(summary.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(summary.getBody().targets()).isNull();
        assertThat(summary.getBody().percentages()).isNull();
    }
}
