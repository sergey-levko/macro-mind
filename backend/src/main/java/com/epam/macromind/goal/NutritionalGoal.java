package com.epam.macromind.goal;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "nutritional_goals")
public class NutritionalGoal {

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

    public NutritionalGoal(UUID userId, BigDecimal caloriesTarget, BigDecimal proteinG,
                           BigDecimal carbsG, BigDecimal fatG) {
        this.userId = userId;
        this.caloriesTarget = caloriesTarget;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fatG = fatG;
    }

    void update(BigDecimal caloriesTarget, BigDecimal proteinG, BigDecimal carbsG, BigDecimal fatG) {
        this.caloriesTarget = caloriesTarget;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fatG = fatG;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public BigDecimal getCaloriesTarget() { return caloriesTarget; }
    public BigDecimal getProteinG() { return proteinG; }
    public BigDecimal getCarbsG() { return carbsG; }
    public BigDecimal getFatG() { return fatG; }
}
