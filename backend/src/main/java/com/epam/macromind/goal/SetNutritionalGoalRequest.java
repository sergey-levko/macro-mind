package com.epam.macromind.goal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

record SetNutritionalGoalRequest(
        @NotNull @Positive BigDecimal caloriesTarget,
        @NotNull @Positive BigDecimal proteinG,
        @NotNull @Positive BigDecimal carbsG,
        @NotNull @Positive BigDecimal fatG
) {}
