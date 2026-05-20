package com.epam.macromind.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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
}
