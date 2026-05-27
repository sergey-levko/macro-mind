package com.epam.macromind.template;

import com.epam.macromind.meal.MealType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "meal_template_items")
class MealTemplateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private MealTemplate template;

    @Column(name = "food_id", nullable = false)
    private UUID foodId;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 50)
    private MealType mealType;

    @Column(name = "quantity_g", nullable = false, precision = 8, scale = 2)
    private BigDecimal quantityG;

    protected MealTemplateItem() {}

    MealTemplateItem(MealTemplate template, UUID foodId, MealType mealType, BigDecimal quantityG) {
        this.template = template;
        this.foodId = foodId;
        this.mealType = mealType;
        this.quantityG = quantityG;
    }

    UUID getId() { return id; }
    MealTemplate getTemplate() { return template; }
    UUID getFoodId() { return foodId; }
    MealType getMealType() { return mealType; }
    BigDecimal getQuantityG() { return quantityG; }
}
