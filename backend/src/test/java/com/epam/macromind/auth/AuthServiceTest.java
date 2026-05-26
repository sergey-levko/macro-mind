package com.epam.macromind.auth;

import com.epam.macromind.user.EmailAlreadyExistsException;
import com.epam.macromind.user.GoalType;
import com.epam.macromind.user.User;
import com.epam.macromind.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    private static final User ALICE = new User("Alice", "alice@example.com", "$2a$hash",
            30, new BigDecimal("65.0"), new BigDecimal("170.0"), GoalType.MAINTAIN_WEIGHT);

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, refreshTokenService);
    }

    @Test
    void register_returnsBothTokens() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$hash");
        when(userRepository.save(any())).thenReturn(ALICE);
        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn("refresh-token");

        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "password1",
                30, new BigDecimal("65.0"), new BigDecimal("170.0"), GoalType.MAINTAIN_WEIGHT);
        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user().email()).isEqualTo("alice@example.com");
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "password1",
                30, new BigDecimal("65.0"), new BigDecimal("170.0"), GoalType.MAINTAIN_WEIGHT);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void login_returnsBothTokens() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(ALICE));
        when(passwordEncoder.matches("password1", "$2a$hash")).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn("refresh-token");

        AuthResponse response = authService.login(new LoginRequest("alice@example.com", "password1"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void login_wrongPassword_throws() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(ALICE));
        when(passwordEncoder.matches("wrong", "$2a$hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refresh_returnsNewTokens() {
        UUID userId = UUID.randomUUID();
        when(refreshTokenService.validateAndRotate("old-refresh"))
                .thenReturn(new RotationResult(userId, "new-refresh"));
        when(jwtService.generateToken(userId)).thenReturn("new-access");

        RefreshResponse response = authService.refresh("old-refresh");

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void logout_delegatesRevocation() {
        authService.logout("some-refresh-token");
        verify(refreshTokenService).revokeByRawToken("some-refresh-token");
    }
}
