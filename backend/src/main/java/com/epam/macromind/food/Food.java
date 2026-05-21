package com.epam.macromind.food;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "foods")
public class Food {

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

    public Food(UUID userId, String name, String source, BigDecimal calories100g,
         BigDecimal proteinG, BigDecimal carbsG, BigDecimal fatG) {
        this.userId = userId;
        this.name = name;
        this.source = source;
        this.calories100g = calories100g;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fatG = fatG;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public String getSource() { return source; }
    public BigDecimal getCalories100g() { return calories100g; }
    public BigDecimal getProteinG() { return proteinG; }
    public BigDecimal getCarbsG() { return carbsG; }
    public BigDecimal getFatG() { return fatG; }

    public void setName(String name) { this.name = name; }
    public void setCalories100g(BigDecimal calories100g) { this.calories100g = calories100g; }
    public void setProteinG(BigDecimal proteinG) { this.proteinG = proteinG; }
    public void setCarbsG(BigDecimal carbsG) { this.carbsG = carbsG; }
    public void setFatG(BigDecimal fatG) { this.fatG = fatG; }
}
