package com.epam.macromind.food;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FoodRepository extends JpaRepository<Food, UUID> {
    Page<Food> findByUserIdOrderByNameAsc(UUID userId, Pageable pageable);
    Page<Food> findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(UUID userId, String name, Pageable pageable);
}
