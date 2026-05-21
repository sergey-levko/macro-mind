package com.epam.macromind.meal;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "meal_items")
public class MealItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_log_id", nullable = false)
    private MealLog mealLog;

    @Column(name = "food_id", nullable = false)
    private UUID foodId;

    @Column(name = "quantity_g", nullable = false, precision = 8, scale = 2)
    private BigDecimal quantityG;

    protected MealItem() {}

    public MealItem(MealLog mealLog, UUID foodId, BigDecimal quantityG) {
        this.mealLog = mealLog;
        this.foodId = foodId;
        this.quantityG = quantityG;
    }

    public UUID getId() { return id; }
    public MealLog getMealLog() { return mealLog; }
    public UUID getFoodId() { return foodId; }
    public BigDecimal getQuantityG() { return quantityG; }
}
