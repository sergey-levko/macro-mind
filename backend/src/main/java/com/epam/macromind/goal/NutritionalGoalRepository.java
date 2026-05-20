package com.epam.macromind.goal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface NutritionalGoalRepository extends JpaRepository<NutritionalGoal, UUID> {

    Optional<NutritionalGoal> findByUserId(UUID userId);
}
