package com.epam.macromind.food;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FoodRepository extends JpaRepository<Food, UUID> {
    List<Food> findTop20ByUserId(UUID userId);
    List<Food> findTop20ByUserIdAndNameContainingIgnoreCase(UUID userId, String name);
}
