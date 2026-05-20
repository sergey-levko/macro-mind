package com.epam.macromind.meal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface MealLogRepository extends JpaRepository<MealLog, UUID> {
    List<MealLog> findByUserIdAndLoggedAtBetween(UUID userId, Instant start, Instant end);
}
