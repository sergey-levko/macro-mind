package com.epam.macromind.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(repository);
    }

    @Test
    void createRefreshToken_storesHashedToken() {
        UUID userId = UUID.randomUUID();
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String rawToken = service.createRefreshToken(userId);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(captor.capture());
        RefreshToken saved = captor.getValue();

        assertThat(rawToken).hasSize(64);
        assertThat(saved.getTokenHash()).isEqualTo(sha256(rawToken));
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.isRevoked()).isFalse();
        assertThat(saved.getExpiresAt()).isAfter(Instant.now().plus(29, ChronoUnit.DAYS));
    }

    @Test
    void validateAndRotate_returnsNewTokensAndRevokesOld() {
        UUID userId = UUID.randomUUID();
        String rawToken = "a".repeat(64);
        String tokenHash = sha256(rawToken);
        RefreshToken existing = new RefreshToken(
                UUID.randomUUID(), userId, tokenHash,
                Instant.now().plus(30, ChronoUnit.DAYS), Instant.now(), false);

        when(repository.findByTokenHash(tokenHash)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RotationResult result = service.validateAndRotate(rawToken);

        assertThat(existing.isRevoked()).isTrue();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.rawRefreshToken()).isNotEqualTo(rawToken);
        assertThat(result.rawRefreshToken()).hasSize(64);
        verify(repository, times(2)).save(any());
    }

    @Test
    void validateAndRotate_throwsOnExpiredToken() {
        String rawToken = "b".repeat(64);
        String tokenHash = sha256(rawToken);
        RefreshToken expired = new RefreshToken(
                UUID.randomUUID(), UUID.randomUUID(), tokenHash,
                Instant.now().minus(1, ChronoUnit.SECONDS), Instant.now(), false);

        when(repository.findByTokenHash(tokenHash)).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.validateAndRotate(rawToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void validateAndRotate_throwsOnRevokedToken() {
        String rawToken = "c".repeat(64);
        String tokenHash = sha256(rawToken);
        RefreshToken revoked = new RefreshToken(
                UUID.randomUUID(), UUID.randomUUID(), tokenHash,
                Instant.now().plus(30, ChronoUnit.DAYS), Instant.now(), true);

        when(repository.findByTokenHash(tokenHash)).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> service.validateAndRotate(rawToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    void revokeByRawToken_isNoOpForUnknownToken() {
        String rawToken = "d".repeat(64);
        when(repository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatCode(() -> service.revokeByRawToken(rawToken)).doesNotThrowAnyException();
        verify(repository, never()).save(any());
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
