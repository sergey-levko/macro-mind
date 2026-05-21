package com.epam.macromind.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void register_and_retrieve_fullRoundTrip() {
        CreateUserRequest request = new CreateUserRequest(
                "Carol", "carol@example.com", 28,
                new BigDecimal("60.0"), new BigDecimal("165.0"), GoalType.LOSE_WEIGHT);

        ResponseEntity<UserResponse> created = restTemplate.postForEntity(
                url("/api/v1/users"), request, UserResponse.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        UUID id = created.getBody().id();
        assertThat(id).isNotNull();
        assertThat(created.getBody().email()).isEqualTo("carol@example.com");

        ResponseEntity<UserResponse> fetched = restTemplate.getForEntity(
                url("/api/v1/users/" + id), UserResponse.class);

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody().name()).isEqualTo("Carol");
        assertThat(fetched.getBody().goalType()).isEqualTo(GoalType.LOSE_WEIGHT);
    }

    @Test
    void register_duplicateEmail_returns409() {
        CreateUserRequest request = new CreateUserRequest(
                "Dave", "dave@example.com", 35,
                new BigDecimal("90.0"), new BigDecimal("185.0"), GoalType.GAIN_MUSCLE);

        restTemplate.postForEntity(url("/api/v1/users"), request, UserResponse.class);

        ResponseEntity<Map> second = restTemplate.postForEntity(
                url("/api/v1/users"), request, Map.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(second.getBody()).containsKey("message");
    }

    @Test
    void getById_unknownId_returns404() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                url("/api/v1/users/" + UUID.randomUUID()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateUser_success_returns200WithUpdatedValues() {
        CreateUserRequest create = new CreateUserRequest(
                "Eve", "eve@example.com", 22,
                new BigDecimal("55.0"), new BigDecimal("160.0"), GoalType.MAINTAIN_WEIGHT);
        UUID id = restTemplate.postForEntity(url("/api/v1/users"), create, UserResponse.class)
                .getBody().id();

        UpdateUserRequest update = new UpdateUserRequest(
                "Eve Updated", 23, new BigDecimal("54.0"), new BigDecimal("160.0"), GoalType.LOSE_WEIGHT);

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                url("/api/v1/users/" + id), HttpMethod.PUT,
                new HttpEntity<>(update), UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Eve Updated");
        assertThat(response.getBody().age()).isEqualTo(23);
        assertThat(response.getBody().goalType()).isEqualTo(GoalType.LOSE_WEIGHT);
        assertThat(response.getBody().email()).isEqualTo("eve@example.com");
    }

    @Test
    void updateUser_unknownId_returns404() {
        UpdateUserRequest update = new UpdateUserRequest(
                "Ghost", 25, new BigDecimal("70.0"), new BigDecimal("175.0"), GoalType.GAIN_MUSCLE);

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/users/" + UUID.randomUUID()), HttpMethod.PUT,
                new HttpEntity<>(update), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateUser_missingRequiredField_returns400() {
        CreateUserRequest create = new CreateUserRequest(
                "Frank", "frank@example.com", 40,
                new BigDecimal("85.0"), new BigDecimal("178.0"), GoalType.GAIN_MUSCLE);
        UUID id = restTemplate.postForEntity(url("/api/v1/users"), create, UserResponse.class)
                .getBody().id();

        UpdateUserRequest invalid = new UpdateUserRequest(
                "", 40, new BigDecimal("85.0"), new BigDecimal("178.0"), GoalType.GAIN_MUSCLE);

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/users/" + id), HttpMethod.PUT,
                new HttpEntity<>(invalid), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
