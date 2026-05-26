package com.epam.macromind.advice;

import com.epam.macromind.AbstractIntegrationTest;
import com.epam.macromind.auth.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AiAdviceIntegrationTest extends AbstractIntegrationTest {

    @TestConfiguration
    static class SyncAsyncConfig {
        @Bean
        @Primary
        TaskExecutor taskExecutor() {
            return new SyncTaskExecutor();
        }
    }

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

    private String register() {
        String body = """
                {
                  "name": "Advice User",
                  "email": "advice-%s@example.com",
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
                new HttpEntity<>(body, headers), AuthResponse.class).getBody().token();
    }

    private void setGoal(String token) {
        String body = "{\"caloriesTarget\":2000,\"proteinG\":150,\"carbsG\":200,\"fatG\":70}";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange(url("/api/v1/nutritional-goals"), HttpMethod.PUT,
                new HttpEntity<>(body, headers), Map.class);
    }

    private HttpHeaders headersFor(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void generateRetrieveList_roundTrip() {
        String token = register();
        setGoal(token);

        String body = "{\"adviceType\":\"DAILY\",\"periodStart\":\"2026-05-20\"}";

        ResponseEntity<AiAdviceResponse> generated = restTemplate.exchange(
                url("/api/v1/advice"), HttpMethod.POST,
                new HttpEntity<>(body, headersFor(token)), AiAdviceResponse.class);
        assertThat(generated.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        UUID adviceId = generated.getBody().id();

        ResponseEntity<AiAdviceResponse> retrieved = restTemplate.exchange(
                url("/api/v1/advice/" + adviceId), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), AiAdviceResponse.class);
        assertThat(retrieved.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(retrieved.getBody().id()).isEqualTo(adviceId);
        assertThat(retrieved.getBody().content()).isEqualTo("You are on track! Keep it up.");
        assertThat(retrieved.getBody().status()).isEqualTo(AdviceStatus.COMPLETED);

        ResponseEntity<AiAdviceResponse[]> listed = restTemplate.exchange(
                url("/api/v1/advice"), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), AiAdviceResponse[].class);
        assertThat(listed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listed.getBody()).hasSize(1);
        assertThat(listed.getBody()[0].id()).isEqualTo(adviceId);
    }

    @Test
    void generateAdvice_duplicateNonPreview_returns200WithSameId() {
        String token = register();
        setGoal(token);

        String body = "{\"adviceType\":\"DAILY\",\"periodStart\":\"2026-05-21\"}";

        ResponseEntity<AiAdviceResponse> first = restTemplate.exchange(
                url("/api/v1/advice"), HttpMethod.POST,
                new HttpEntity<>(body, headersFor(token)), AiAdviceResponse.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        UUID firstId = first.getBody().id();

        ResponseEntity<AiAdviceResponse> second = restTemplate.exchange(
                url("/api/v1/advice"), HttpMethod.POST,
                new HttpEntity<>(body, headersFor(token)), AiAdviceResponse.class);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(second.getBody().id()).isEqualTo(firstId);
    }

    @Test
    void generateAdvice_noGoal_returns400() {
        String token = register();
        String body = "{\"adviceType\":\"DAILY\",\"periodStart\":\"2026-05-20\"}";

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/advice"), HttpMethod.POST,
                new HttpEntity<>(body, headersFor(token)), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
