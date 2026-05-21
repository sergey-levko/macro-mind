package com.epam.macromind.meal;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateMealLogRequest(
        @NotNull MealType mealType,
        Instant loggedAt
) {}
