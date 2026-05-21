package com.epam.macromind.dashboard;

import com.epam.macromind.food.CreateFoodRequest;
import com.epam.macromind.food.FoodResponse;
import com.epam.macromind.meal.CreateMealLogRequest;
import com.epam.macromind.meal.MealLogResponse;
import com.epam.macromind.meal.MealType;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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

    private HttpHeaders headersFor(UUID userId) {
        HttpHeaders h = new HttpHeaders();
        h.set("X-User-Id", userId.toString());
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private UUID createUser() {
        var req = new CreateUserRequest("Dash User", "dash-" + UUID.randomUUID() + "@test.com",
                30, new BigDecimal("75"), new BigDecimal("180"), GoalType.MAINTAIN_WEIGHT);
        return restTemplate.postForEntity(url("/api/v1/users"), req, UserResponse.class).getBody().id();
    }

    private void setGoal(UUID userId) {
        String body = "{\"caloriesTarget\":2000,\"proteinG\":150,\"carbsG\":200,\"fatG\":70}";
        HttpHeaders h = headersFor(userId);
        restTemplate.exchange(url("/api/v1/nutritional-goals"), HttpMethod.PUT,
                new HttpEntity<>(body, h), Map.class);
    }

    private UUID createFood(UUID userId) {
        var req = new CreateFoodRequest("Rice", new BigDecimal("130"),
                new BigDecimal("3"), new BigDecimal("28"), new BigDecimal("0.3"));
        return restTemplate.exchange(url("/api/v1/foods"), HttpMethod.POST,
                new HttpEntity<>(req, headersFor(userId)), FoodResponse.class).getBody().id();
    }

    private void createMealLog(UUID userId, UUID foodId, String date) {
        String body = String.format(
                "{\"mealType\":\"LUNCH\",\"loggedAt\":\"%sT12:00:00Z\",\"items\":[{\"foodId\":\"%s\",\"quantityG\":200}]}",
                date, foodId);
        restTemplate.exchange(url("/api/v1/meal-logs"), HttpMethod.POST,
                new HttpEntity<>(body, headersFor(userId)), MealLogResponse.class);
    }

    @Test
    void dailyWeeklySummary_correctAggregatedTotals() {
        UUID userId = createUser();
        setGoal(userId);
        UUID foodId = createFood(userId);
        String today = LocalDate.now().toString();
        createMealLog(userId, foodId, today);

        // daily
        ResponseEntity<DailyDashboardResponse> daily = restTemplate.exchange(
                url("/api/v1/dashboard/daily?date=" + today), HttpMethod.GET,
                new HttpEntity<>(headersFor(userId)), DailyDashboardResponse.class);
        assertThat(daily.getStatusCode()).isEqualTo(HttpStatus.OK);
        // 200g of rice at 130kcal/100g = 260 kcal
        assertThat(daily.getBody().totals().caloriesKcal()).isEqualByComparingTo("260.00");
        assertThat(daily.getBody().targets().caloriesTarget()).isEqualByComparingTo("2000");

        // weekly — today is in the window
        String weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY).toString();
        ResponseEntity<WeeklyDashboardResponse> weekly = restTemplate.exchange(
                url("/api/v1/dashboard/weekly?weekStart=" + weekStart), HttpMethod.GET,
                new HttpEntity<>(headersFor(userId)), WeeklyDashboardResponse.class);
        assertThat(weekly.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(weekly.getBody().days()).hasSize(7);
        assertThat(weekly.getBody().weeklyTargets().caloriesTarget()).isEqualByComparingTo("14000");

        // summary card
        ResponseEntity<SummaryDashboardResponse> summary = restTemplate.exchange(
                url("/api/v1/dashboard/summary"), HttpMethod.GET,
                new HttpEntity<>(headersFor(userId)), SummaryDashboardResponse.class);
        assertThat(summary.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(summary.getBody().percentages()).isNotNull();
        assertThat(summary.getBody().percentages().caloriesPct()).isEqualTo(13); // 260/2000=13%
    }

    @Test
    void summary_noGoal_returnsNullTargetsAndPercentages() {
        UUID userId = createUser();

        ResponseEntity<SummaryDashboardResponse> summary = restTemplate.exchange(
                url("/api/v1/dashboard/summary"), HttpMethod.GET,
                new HttpEntity<>(headersFor(userId)), SummaryDashboardResponse.class);

        assertThat(summary.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(summary.getBody().targets()).isNull();
        assertThat(summary.getBody().percentages()).isNull();
    }
}
