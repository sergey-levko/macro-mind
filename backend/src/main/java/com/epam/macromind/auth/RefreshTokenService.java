package com.epam.macromind.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Service
class RefreshTokenService {

    private static final int TOKEN_BYTES = 32;
    private static final long EXPIRY_DAYS = 30;

    private final RefreshTokenRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();

    RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Transactional
    String createRefreshToken(UUID userId) {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        String rawToken = HexFormat.of().formatHex(bytes);
        String tokenHash = sha256(rawToken);

        Instant now = Instant.now();
        RefreshToken entity = new RefreshToken(
                UUID.randomUUID(),
                userId,
                tokenHash,
                now.plus(EXPIRY_DAYS, ChronoUnit.DAYS),
                now,
                false
        );
        repository.save(entity);
        return rawToken;
    }

    @Transactional
    RotationResult validateAndRotate(String rawToken) {
        String tokenHash = sha256(rawToken);
        RefreshToken existing = repository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));

        if (existing.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }
        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        existing.setRevoked(true);
        repository.save(existing);

        String newRawToken = createRefreshToken(existing.getUserId());
        return new RotationResult(existing.getUserId(), newRawToken);
    }

    @Transactional
    void revokeByRawToken(String rawToken) {
        String tokenHash = sha256(rawToken);
        repository.findByTokenHash(tokenHash).ifPresent(t -> {
            t.setRevoked(true);
            repository.save(t);
        });
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
