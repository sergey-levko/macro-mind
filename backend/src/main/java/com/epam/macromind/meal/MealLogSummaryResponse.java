package com.epam.macromind.meal;

import java.time.Instant;
import java.util.UUID;

public record MealLogSummaryResponse(
        UUID id,
        MealType mealType,
        Instant loggedAt,
        MacroTotals totals
) {}
