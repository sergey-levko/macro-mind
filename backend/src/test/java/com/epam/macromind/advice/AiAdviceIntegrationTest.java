package com.epam.macromind.advice;

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
class AiAdviceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("usda.api.base-url", () -> "http://localhost:9999");
    }

    @MockitoBean(name = "aiAdviceChatClient")
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
        when(requestSpec.system(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("You are on track! Keep it up.");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private UUID createUser() {
        CreateUserRequest req = new CreateUserRequest(
                "Advice User", "advice-" + UUID.randomUUID() + "@example.com",
                28, new BigDecimal("70.0"), new BigDecimal("175.0"), GoalType.MAINTAIN_WEIGHT);
        return restTemplate.postForEntity(url("/api/v1/users"), req, UserResponse.class)
                .getBody().id();
    }

    private void setGoal(UUID userId) {
        String body = "{\"caloriesTarget\":2000,\"proteinG\":150,\"carbsG\":200,\"fatG\":70}";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange(url("/api/v1/nutritional-goals"), HttpMethod.PUT,
                new HttpEntity<>(body, headers), Map.class);
    }

    private HttpHeaders headersFor(UUID userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void generateRetrieveList_roundTrip() {
        UUID userId = createUser();
        setGoal(userId);

        String body = "{\"adviceType\":\"DAILY\",\"periodStart\":\"2026-05-20\"}";

        ResponseEntity<AiAdviceResponse> generated = restTemplate.exchange(
                url("/api/v1/advice"), HttpMethod.POST,
                new HttpEntity<>(body, headersFor(userId)), AiAdviceResponse.class);
        assertThat(generated.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(generated.getBody().content()).isEqualTo("You are on track! Keep it up.");

        UUID adviceId = generated.getBody().id();

        ResponseEntity<AiAdviceResponse> retrieved = restTemplate.getForEntity(
                url("/api/v1/advice/" + adviceId), AiAdviceResponse.class);
        assertThat(retrieved.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(retrieved.getBody().id()).isEqualTo(adviceId);

        ResponseEntity<AiAdviceResponse[]> listed = restTemplate.exchange(
                url("/api/v1/advice"), HttpMethod.GET,
                new HttpEntity<>(headersFor(userId)), AiAdviceResponse[].class);
        assertThat(listed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listed.getBody()).hasSize(1);
        assertThat(listed.getBody()[0].id()).isEqualTo(adviceId);
    }

    @Test
    void generateAdvice_noGoal_returns400() {
        UUID userId = createUser();
        String body = "{\"adviceType\":\"DAILY\",\"periodStart\":\"2026-05-20\"}";

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/advice"), HttpMethod.POST,
                new HttpEntity<>(body, headersFor(userId)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
