package com.epam.macromind.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    boolean existsByTokenHashAndRevokedFalseAndExpiresAtAfter(String tokenHash, Instant now);
}
