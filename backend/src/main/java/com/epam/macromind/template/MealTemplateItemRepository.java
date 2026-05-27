package com.epam.macromind.template;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface MealTemplateItemRepository extends JpaRepository<MealTemplateItem, UUID> {
}
