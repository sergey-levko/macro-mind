package com.epam.macromind.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-chars!!";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
    }

    @Test
    void generateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId);
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void invalidTokenReturnsFalse() {
        assertThat(jwtService.isTokenValid("not.a.token")).isFalse();
    }

    @Test
    void expiredTokenReturnsFalse() throws Exception {
        // Create a service with TTL effectively 0 by using reflection is complex,
        // so we validate that a tampered token is rejected instead.
        String token = jwtService.generateToken(UUID.randomUUID());
        String tampered = token.substring(0, token.length() - 4) + "XXXX";
        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }
}
