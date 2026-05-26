package com.epam.macromind.auth;

import com.epam.macromind.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private String registerBody(String suffix) {
        return """
                {
                  "name": "Test User",
                  "email": "user-%s@example.com",
                  "password": "password123",
                  "age": 30,
                  "weightKg": 75.0,
                  "heightCm": 180.0,
                  "goalType": "MAINTAIN_WEIGHT"
                }
                """.formatted(suffix);
    }

    @Test
    void register_success_returns201WithBothTokens() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>(registerBody(UUID.randomUUID().toString()), headers);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                url("/api/v1/auth/register"), req, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().refreshToken()).isNotBlank();
        assertThat(response.getBody().user().email()).contains("@example.com");
    }

    @Test
    void register_duplicateEmail_returns409() {
        String body = registerBody("duplicate-" + UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(url("/api/v1/auth/register"), new HttpEntity<>(body, headers), AuthResponse.class);
        ResponseEntity<Map> second = restTemplate.postForEntity(
                url("/api/v1/auth/register"), new HttpEntity<>(body, headers), Map.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void register_missingPassword_returns400() {
        String body = """
                {"name":"Test","email":"a@b.com","age":30,"weightKg":75,"heightCm":180,"goalType":"LOSE_WEIGHT"}
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/register"), new HttpEntity<>(body, headers), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_success_returns200WithBothTokens() {
        String suffix = UUID.randomUUID().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(url("/api/v1/auth/register"),
                new HttpEntity<>(registerBody(suffix), headers), AuthResponse.class);

        String loginBody = """
                {"email": "user-%s@example.com", "password": "password123"}
                """.formatted(suffix);
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                url("/api/v1/auth/login"), new HttpEntity<>(loginBody, headers), AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().refreshToken()).isNotBlank();
    }

    @Test
    void login_wrongPassword_returns401() {
        String suffix = UUID.randomUUID().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(url("/api/v1/auth/register"),
                new HttpEntity<>(registerBody(suffix), headers), AuthResponse.class);

        String loginBody = """
                {"email": "user-%s@example.com", "password": "wrongpassword"}
                """.formatted(suffix);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/login"), new HttpEntity<>(loginBody, headers), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_unknownEmail_returns401() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String loginBody = """
                {"email": "nobody@example.com", "password": "password123"}
                """;

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/login"), new HttpEntity<>(loginBody, headers), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedEndpoint_noToken_returns401() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                url("/api/v1/users/me"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void fullFlow_registerLoginRefreshLogout() {
        String suffix = UUID.randomUUID().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Register
        ResponseEntity<AuthResponse> registerResp = restTemplate.postForEntity(
                url("/api/v1/auth/register"),
                new HttpEntity<>(registerBody(suffix), headers), AuthResponse.class);
        assertThat(registerResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String refreshToken = registerResp.getBody().refreshToken();
        assertThat(refreshToken).isNotBlank();

        // Refresh — get new tokens
        String refreshBody = "{\"refreshToken\":\"%s\"}".formatted(refreshToken);
        ResponseEntity<RefreshResponse> refreshResp = restTemplate.postForEntity(
                url("/api/v1/auth/refresh"),
                new HttpEntity<>(refreshBody, headers), RefreshResponse.class);
        assertThat(refreshResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String newAccessToken = refreshResp.getBody().accessToken();
        String newRefreshToken = refreshResp.getBody().refreshToken();
        assertThat(newAccessToken).isNotBlank();
        assertThat(newRefreshToken).isNotBlank();
        assertThat(newRefreshToken).isNotEqualTo(refreshToken);

        // Old refresh token is now revoked — second use returns 401
        ResponseEntity<Map> reuseResp = restTemplate.postForEntity(
                url("/api/v1/auth/refresh"),
                new HttpEntity<>(refreshBody, headers), Map.class);
        assertThat(reuseResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Logout using new refresh token (authenticated)
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        authHeaders.setBearerAuth(newAccessToken);
        String logoutBody = "{\"refreshToken\":\"%s\"}".formatted(newRefreshToken);
        ResponseEntity<Void> logoutResp = restTemplate.postForEntity(
                url("/api/v1/auth/logout"),
                new HttpEntity<>(logoutBody, authHeaders), Void.class);
        assertThat(logoutResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // After logout, the new refresh token is revoked
        String newRefreshBody = "{\"refreshToken\":\"%s\"}".formatted(newRefreshToken);
        ResponseEntity<Map> afterLogout = restTemplate.postForEntity(
                url("/api/v1/auth/refresh"),
                new HttpEntity<>(newRefreshBody, headers), Map.class);
        assertThat(afterLogout.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
