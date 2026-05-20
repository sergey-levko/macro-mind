package com.epam.macromind.meal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

record AddMealItemRequest(
        @NotNull UUID foodId,
        @NotNull @Positive BigDecimal quantityG
) {}
