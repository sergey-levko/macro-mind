package com.epam.macromind.meal;

import java.math.BigDecimal;
import java.util.UUID;

record MealItemResponse(
        UUID itemId,
        UUID foodId,
        String foodName,
        BigDecimal quantityG,
        BigDecimal calories,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG
) {}
