package com.epam.macromind.settings;

import com.epam.macromind.AbstractIntegrationTest;
import com.epam.macromind.auth.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserSettingsIntegrationTest extends AbstractIntegrationTest {

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
                  "name": "Settings User",
                  "email": "settings-%s@example.com",
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
                new HttpEntity<>(body, headers), AuthResponse.class).getBody().accessToken();
    }

    private HttpHeaders headersFor(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void getSettings_defaultIsTrue() {
        String token = register();

        ResponseEntity<UserSettingsResponse> response = restTemplate.exchange(
                url("/api/v1/settings"), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), UserSettingsResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().usdaEnabled()).isTrue();
    }

    @Test
    void toggleOff_getReflectsChange() {
        String token = register();

        ResponseEntity<UserSettingsResponse> putResponse = restTemplate.exchange(
                url("/api/v1/settings"), HttpMethod.PUT,
                new HttpEntity<>("{\"usdaEnabled\":false}", headersFor(token)), UserSettingsResponse.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody().usdaEnabled()).isFalse();

        ResponseEntity<UserSettingsResponse> getResponse = restTemplate.exchange(
                url("/api/v1/settings"), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), UserSettingsResponse.class);
        assertThat(getResponse.getBody().usdaEnabled()).isFalse();
    }

    @Test
    void toggleOffThenOn_getReflectsTrue() {
        String token = register();

        restTemplate.exchange(url("/api/v1/settings"), HttpMethod.PUT,
                new HttpEntity<>("{\"usdaEnabled\":false}", headersFor(token)), UserSettingsResponse.class);
        restTemplate.exchange(url("/api/v1/settings"), HttpMethod.PUT,
                new HttpEntity<>("{\"usdaEnabled\":true}", headersFor(token)), UserSettingsResponse.class);

        ResponseEntity<UserSettingsResponse> getResponse = restTemplate.exchange(
                url("/api/v1/settings"), HttpMethod.GET,
                new HttpEntity<>(headersFor(token)), UserSettingsResponse.class);
        assertThat(getResponse.getBody().usdaEnabled()).isTrue();
    }

    @Test
    void putSettings_missingBody_returns400() {
        String token = register();

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/settings"), HttpMethod.PUT,
                new HttpEntity<>("{}", headersFor(token)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
