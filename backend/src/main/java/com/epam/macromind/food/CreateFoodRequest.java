package com.epam.macromind.food;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

record CreateFoodRequest(
        @NotBlank String name,
        @NotNull BigDecimal calories100g,
        @NotNull BigDecimal proteinG,
        @NotNull BigDecimal carbsG,
        @NotNull BigDecimal fatG
) {}
