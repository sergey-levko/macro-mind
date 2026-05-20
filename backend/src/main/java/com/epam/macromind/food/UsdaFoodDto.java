package com.epam.macromind.food;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record UsdaFoodDto(String description, List<FoodNutrient> foodNutrients) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FoodNutrient(Nutrient nutrient, BigDecimal amount) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Nutrient(Integer id) {}

    BigDecimal getNutrientAmount(int nutrientId) {
        if (foodNutrients == null) return BigDecimal.ZERO;
        return foodNutrients.stream()
                .filter(n -> n.nutrient() != null && nutrientId == n.nutrient().id())
                .map(FoodNutrient::amount)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
}
