package com.epam.macromind.goal;

import com.epam.macromind.user.CreateUserRequest;
import com.epam.macromind.user.GoalType;
import com.epam.macromind.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class NutritionalGoalIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

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

    private UUID createUser() {
        CreateUserRequest req = new CreateUserRequest(
                "Goal User", "goal-" + UUID.randomUUID() + "@example.com",
                30, new BigDecimal("75.0"), new BigDecimal("180.0"), GoalType.LOSE_WEIGHT);
        return restTemplate.postForEntity(url("/api/v1/users"), req, UserResponse.class)
                .getBody().id();
    }

    private HttpHeaders headersFor(UUID userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private static final String GOAL_BODY =
            "{\"caloriesTarget\":2000,\"proteinG\":150,\"carbsG\":200,\"fatG\":70}";

    @Test
    void fullRoundTrip_setGetReplaceDelete() {
        UUID userId = createUser();

        ResponseEntity<NutritionalGoalResponse> set = restTemplate.exchange(
                url("/api/v1/nutritional-goals"), HttpMethod.PUT,
                new HttpEntity<>(GOAL_BODY, headersFor(userId)), NutritionalGoalResponse.class);
        assertThat(set.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(set.getBody().caloriesTarget()).isEqualByComparingTo("2000");

        ResponseEntity<NutritionalGoalResponse> get = restTemplate.exchange(
                url("/api/v1/nutritional-goals"), HttpMethod.GET,
                new HttpEntity<>(headersFor(userId)), NutritionalGoalResponse.class);
        assertThat(get.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(get.getBody().id()).isEqualTo(set.getBody().id());

        String updatedBody = "{\"caloriesTarget\":2500,\"proteinG\":180,\"carbsG\":220,\"fatG\":80}";
        ResponseEntity<NutritionalGoalResponse> replaced = restTemplate.exchange(
                url("/api/v1/nutritional-goals"), HttpMethod.PUT,
                new HttpEntity<>(updatedBody, headersFor(userId)), NutritionalGoalResponse.class);
        assertThat(replaced.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(replaced.getBody().caloriesTarget()).isEqualByComparingTo("2500");

        ResponseEntity<Void> deleted = restTemplate.exchange(
                url("/api/v1/nutritional-goals"), HttpMethod.DELETE,
                new HttpEntity<>(headersFor(userId)), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Map> afterDelete = restTemplate.exchange(
                url("/api/v1/nutritional-goals"), HttpMethod.GET,
                new HttpEntity<>(headersFor(userId)), Map.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getGoal_noGoalSet_returns404() {
        UUID userId = createUser();

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/nutritional-goals"), HttpMethod.GET,
                new HttpEntity<>(headersFor(userId)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteGoal_noGoalSet_returns404() {
        UUID userId = createUser();

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/nutritional-goals"), HttpMethod.DELETE,
                new HttpEntity<>(headersFor(userId)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void generateGoal_validUser_returns200WithNumericFields() {
        UUID userId = createUser();

        ResponseEntity<GoalSuggestionResponse> response = restTemplate.exchange(
                url("/api/v1/nutritional-goals/generate"), HttpMethod.POST,
                new HttpEntity<>(headersFor(userId)), GoalSuggestionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().caloriesTarget()).isEqualByComparingTo("2000");
        assertThat(response.getBody().proteinG()).isEqualByComparingTo("150");
        assertThat(response.getBody().carbsG()).isEqualByComparingTo("200");
        assertThat(response.getBody().fatG()).isEqualByComparingTo("70");
    }

    @Test
    void generateGoal_unknownUser_returns404() {
        UUID unknownId = UUID.randomUUID();

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/nutritional-goals/generate"), HttpMethod.POST,
                new HttpEntity<>(headersFor(unknownId)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
