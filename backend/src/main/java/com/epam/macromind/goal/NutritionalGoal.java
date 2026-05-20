package com.epam.macromind.goal;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "nutritional_goals")
class NutritionalGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "calories_target", nullable = false)
    private BigDecimal caloriesTarget;

    @Column(name = "protein_g", nullable = false)
    private BigDecimal proteinG;

    @Column(name = "carbs_g", nullable = false)
    private BigDecimal carbsG;

    @Column(name = "fat_g", nullable = false)
    private BigDecimal fatG;

    protected NutritionalGoal() {}

    NutritionalGoal(UUID userId, BigDecimal caloriesTarget, BigDecimal proteinG,
                    BigDecimal carbsG, BigDecimal fatG) {
        this.userId = userId;
        this.caloriesTarget = caloriesTarget;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fatG = fatG;
    }

    UUID getId() { return id; }
    UUID getUserId() { return userId; }
    BigDecimal getCaloriesTarget() { return caloriesTarget; }
    BigDecimal getProteinG() { return proteinG; }
    BigDecimal getCarbsG() { return carbsG; }
    BigDecimal getFatG() { return fatG; }
}
