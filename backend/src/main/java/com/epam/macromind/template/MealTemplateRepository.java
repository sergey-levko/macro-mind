package com.epam.macromind.template;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface MealTemplateRepository extends JpaRepository<MealTemplate, UUID> {
    List<MealTemplate> findByUserId(UUID userId);
}
