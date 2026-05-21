package com.epam.macromind.meal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MealItemRepository extends JpaRepository<MealItem, UUID> {
    boolean existsByFoodId(UUID foodId);
}
