package com.epam.macromind.goal;

import java.math.BigDecimal;
import java.util.UUID;

record NutritionalGoalResponse(
        UUID id,
        UUID userId,
        BigDecimal caloriesTarget,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG
) {}
