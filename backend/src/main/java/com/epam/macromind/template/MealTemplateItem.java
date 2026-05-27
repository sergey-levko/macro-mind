package com.epam.macromind.template;

import jakarta.persistence.*;
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

    protected MealTemplateItem() {}

    MealTemplateItem(MealTemplate template, UUID foodId) {
        this.template = template;
        this.foodId = foodId;
    }

    UUID getId() { return id; }
    MealTemplate getTemplate() { return template; }
    UUID getFoodId() { return foodId; }
}
