package com.epam.macromind.food;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FoodRepository extends JpaRepository<Food, UUID> {
    Page<Food> findByUserIdOrderByNameAsc(UUID userId, Pageable pageable);
    Page<Food> findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(UUID userId, String name, Pageable pageable);

    @Query(value = """
            SELECT f.*
            FROM foods f
            JOIN (
                SELECT mi.food_id, MAX(ml.logged_at) AS last_used
                FROM meal_items mi
                JOIN meal_logs ml ON mi.meal_log_id = ml.id
                WHERE ml.user_id = :userId
                GROUP BY mi.food_id
                ORDER BY last_used DESC
                LIMIT :limit
            ) recent ON f.id = recent.food_id
            ORDER BY recent.last_used DESC
            """, nativeQuery = true)
    List<Food> findRecentByUserId(@Param("userId") UUID userId, @Param("limit") int limit);
}
