package com.epam.macromind.meal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MealLogRepository extends JpaRepository<MealLog, UUID> {
    List<MealLog> findByUserIdAndLoggedAtGreaterThanEqualAndLoggedAtLessThan(UUID userId, Instant start, Instant end);

    @Query("SELECT m FROM MealLog m LEFT JOIN FETCH m.items WHERE m.userId = :userId AND m.loggedAt >= :start AND m.loggedAt < :end")
    List<MealLog> findWithItemsByUserIdAndLoggedAtBetween(@Param("userId") UUID userId, @Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT DISTINCT m.userId FROM MealLog m WHERE m.loggedAt >= :start AND m.loggedAt < :end")
    List<UUID> findDistinctUserIdsByLoggedAtBetween(@Param("start") Instant start, @Param("end") Instant end);
}
