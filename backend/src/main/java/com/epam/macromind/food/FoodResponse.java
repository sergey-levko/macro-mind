package com.epam.macromind.food;

import java.math.BigDecimal;
import java.util.UUID;

record FoodResponse(
        UUID id,
        String name,
        String source,
        BigDecimal calories100g,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG
) {
    static FoodResponse from(Food f) {
        return new FoodResponse(f.getId(), f.getName(), f.getSource(),
                f.getCalories100g(), f.getProteinG(), f.getCarbsG(), f.getFatG());
    }
}
