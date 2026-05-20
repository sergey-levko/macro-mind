package com.epam.macromind.goal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

record SetNutritionalGoalRequest(
        @NotNull @DecimalMin(value = "0", exclusive = true) BigDecimal caloriesTarget,
        @NotNull @DecimalMin(value = "0", exclusive = true) BigDecimal proteinG,
        @NotNull @DecimalMin(value = "0", exclusive = true) BigDecimal carbsG,
        @NotNull @DecimalMin(value = "0", exclusive = true) BigDecimal fatG
) {}
