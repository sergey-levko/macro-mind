package com.epam.macromind.meal;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MealLogResponse(
        UUID id,
        UUID userId,
        MealType mealType,
        Instant loggedAt,
        List<MealItemResponse> items,
        MacroTotals totals
) {}
