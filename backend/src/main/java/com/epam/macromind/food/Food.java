package com.epam.macromind.food;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "foods")
class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "calories_100g", precision = 7, scale = 2)
    private BigDecimal calories100g;

    @Column(name = "protein_g", precision = 6, scale = 2)
    private BigDecimal proteinG;

    @Column(name = "carbs_g", precision = 6, scale = 2)
    private BigDecimal carbsG;

    @Column(name = "fat_g", precision = 6, scale = 2)
    private BigDecimal fatG;

    protected Food() {}

    Food(UUID userId, String name, String source, BigDecimal calories100g,
         BigDecimal proteinG, BigDecimal carbsG, BigDecimal fatG) {
        this.userId = userId;
        this.name = name;
        this.source = source;
        this.calories100g = calories100g;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fatG = fatG;
    }

    UUID getId() { return id; }
    UUID getUserId() { return userId; }
    String getName() { return name; }
    String getSource() { return source; }
    BigDecimal getCalories100g() { return calories100g; }
    BigDecimal getProteinG() { return proteinG; }
    BigDecimal getCarbsG() { return carbsG; }
    BigDecimal getFatG() { return fatG; }
}
