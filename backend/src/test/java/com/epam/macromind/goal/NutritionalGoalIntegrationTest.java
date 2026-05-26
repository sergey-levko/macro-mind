package com.epam.macromind.goal;

import com.epam.macromind.AbstractIntegrationTest;
import com.epam.macromind.auth.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NutritionalGoalIntegrationTest extends AbstractIntegrationTest {

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("usda.api.base-url", () -> "http://localhost:9999");
    }

    @MockitoBean(name = "goalChatClient")
    ChatClient chatClient;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUpChatClient() {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(GoalSuggestionResponse.class)).thenReturn(
                new GoalSuggestionResponse(
                        new BigDecimal("2000"), new BigDecimal("150"),
                        new BigDecimal("200"), new BigDecimal("70")));
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private String register() {
        String body = """
                {
                  "name": "Goal User",
                  "email": "goal-%s@example.com",
                  "password": "password123",
                  "age": 30,
                  "weightKg": 75.0,
                  "heightCm": 180.0,
                  "goalType": "LOSE_WEIGHT"
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

    private static final String GOAL_BODY =
            "{\"caloriesTarget\":2000,\"proteinG\":150,\"carbsG\":200,\"fatG\":70}";

    @Test
    void fullRoundTrip_setGetReplaceDelete() {
        String token = register();

        ResponseEntity<NutritionalGoalResponse> set = restTemplate.exchange(
                url("/api/v1/nutritional-goals"), HttpMethod.PUT,
                new HttpEntity<>(GOAL_BODY, headersFor(token)), NutritionalGoalResponse.class);
        assertThat(set.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(set.getBody().caloriesTarget()).isEqualByComparingTo("2000");

        ResponseEntity<NutritionalGoalResponse> get = restTemplate.exchange(
                url("/api/v1/nutritional-goals"), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), NutritionalGoalResponse.class);
        assertThat(get.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(get.getBody().id()).isEqualTo(set.getBody().id());

        String updatedBody = "{\"caloriesTarget\":2500,\"proteinG\":180,\"carbsG\":220,\"fatG\":80}";
        ResponseEntity<NutritionalGoalResponse> replaced = restTemplate.exchange(
                url("/api/v1/nutritional-goals"), HttpMethod.PUT,
                new HttpEntity<>(updatedBody, headersFor(token)), NutritionalGoalResponse.class);
        assertThat(replaced.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(replaced.getBody().caloriesTarget()).isEqualByComparingTo("2500");

        ResponseEntity<Void> deleted = restTemplate.exchange(
                url("/api/v1/nutritional-goals"), HttpMethod.DELETE,
                new HttpEntity<>(headersFor(token)), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Map> afterDelete = restTemplate.exchange(
                url("/api/v1/nutritional-goals"), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), Map.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getGoal_noGoalSet_returns404() {
        String token = register();

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/nutritional-goals"), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteGoal_noGoalSet_returns404() {
        String token = register();

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/nutritional-goals"), HttpMethod.DELETE,
                new HttpEntity<>(headersFor(token)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void generateGoal_validUser_returns200WithNumericFields() {
        String token = register();

        ResponseEntity<GoalSuggestionResponse> response = restTemplate.exchange(
                url("/api/v1/nutritional-goals/generate"), HttpMethod.POST,
                new HttpEntity<>(headersFor(token)), GoalSuggestionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().caloriesTarget()).isEqualByComparingTo("2000");
        assertThat(response.getBody().proteinG()).isEqualByComparingTo("150");
        assertThat(response.getBody().carbsG()).isEqualByComparingTo("200");
        assertThat(response.getBody().fatG()).isEqualByComparingTo("70");
    }
}
