package com.epam.macromind.meal;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

record CreateMealLogRequest(
        @NotNull MealType mealType,
        Instant loggedAt
) {}
