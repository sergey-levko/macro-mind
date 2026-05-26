package com.epam.macromind.user;

import com.epam.macromind.AbstractIntegrationTest;
import com.epam.macromind.auth.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private AuthResponse register(String suffix) {
        String body = """
                {
                  "name": "Test User",
                  "email": "user-%s@example.com",
                  "password": "password123",
                  "age": 30,
                  "weightKg": 65.0,
                  "heightCm": 170.0,
                  "goalType": "MAINTAIN_WEIGHT"
                }
                """.formatted(suffix);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity(url("/api/v1/auth/register"),
                new HttpEntity<>(body, headers), AuthResponse.class).getBody();
    }

    private HttpHeaders headersFor(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void register_and_retrieve_fullRoundTrip() {
        AuthResponse auth = register(UUID.randomUUID().toString());
        assertThat(auth.accessToken()).isNotBlank();
        assertThat(auth.user().email()).contains("@example.com");

        ResponseEntity<UserResponse> fetched = restTemplate.exchange(
                url("/api/v1/users/me"), HttpMethod.GET,
                new HttpEntity<>(headersFor(auth.accessToken())), UserResponse.class);

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody().id()).isEqualTo(auth.user().id());
    }

    @Test
    void register_duplicateEmail_returns409() {
        String suffix = "dup-" + UUID.randomUUID();
        register(suffix);

        String body = """
                {
                  "name": "Test User",
                  "email": "user-%s@example.com",
                  "password": "password123",
                  "age": 30,
                  "weightKg": 65.0,
                  "heightCm": 170.0,
                  "goalType": "MAINTAIN_WEIGHT"
                }
                """.formatted(suffix);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> second = restTemplate.postForEntity(
                url("/api/v1/auth/register"), new HttpEntity<>(body, headers), Map.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(second.getBody()).containsKey("message");
    }

    @Test
    void updateUser_success_returns200WithUpdatedValues() {
        AuthResponse auth = register(UUID.randomUUID().toString());

        UpdateUserRequest update = new UpdateUserRequest(
                "Updated Name", 23, new BigDecimal("54.0"), new BigDecimal("160.0"), GoalType.LOSE_WEIGHT);

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                url("/api/v1/users/me"), HttpMethod.PUT,
                new HttpEntity<>(update, headersFor(auth.accessToken())), UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Updated Name");
        assertThat(response.getBody().age()).isEqualTo(23);
        assertThat(response.getBody().goalType()).isEqualTo(GoalType.LOSE_WEIGHT);
    }

    @Test
    void updateUser_missingRequiredField_returns400() {
        AuthResponse auth = register(UUID.randomUUID().toString());

        UpdateUserRequest invalid = new UpdateUserRequest(
                "", 40, new BigDecimal("85.0"), new BigDecimal("178.0"), GoalType.GAIN_MUSCLE);

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/users/me"), HttpMethod.PUT,
                new HttpEntity<>(invalid, headersFor(auth.accessToken())), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getMe_noToken_returns401() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                url("/api/v1/users/me"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void updatePassword_success_returns204AndLoginWorksWithNewPassword() {
        AuthResponse auth = register(UUID.randomUUID().toString());
        String email = auth.user().email();

        String body = "{\"currentPassword\":\"password123\",\"newPassword\":\"newpassword456\"}";
        ResponseEntity<Void> updated = restTemplate.exchange(
                url("/api/v1/users/me/password"), HttpMethod.PUT,
                new HttpEntity<>(body, headersFor(auth.accessToken())), Void.class);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String loginBody = "{\"email\":\"" + email + "\",\"password\":\"newpassword456\"}";
        ResponseEntity<Map> login = restTemplate.exchange(
                url("/api/v1/auth/login"), HttpMethod.POST,
                new HttpEntity<>(loginBody, headers), Map.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login.getBody()).containsKey("token");
    }

    @Test
    void updatePassword_wrongCurrentPassword_returns401() {
        AuthResponse auth = register(UUID.randomUUID().toString());

        String body = "{\"currentPassword\":\"wrongpassword\",\"newPassword\":\"newpassword456\"}";
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/users/me/password"), HttpMethod.PUT,
                new HttpEntity<>(body, headersFor(auth.accessToken())), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void updatePassword_newPasswordTooShort_returns400() {
        AuthResponse auth = register(UUID.randomUUID().toString());

        String body = "{\"currentPassword\":\"password123\",\"newPassword\":\"short\"}";
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/users/me/password"), HttpMethod.PUT,
                new HttpEntity<>(body, headersFor(auth.accessToken())), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updatePassword_noToken_returns401() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"currentPassword\":\"password123\",\"newPassword\":\"newpassword456\"}";
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/users/me/password"), HttpMethod.PUT,
                new HttpEntity<>(body, headers), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
