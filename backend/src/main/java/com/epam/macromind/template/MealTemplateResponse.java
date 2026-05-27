package com.epam.macromind.template;

import com.epam.macromind.meal.MacroTotals;

import java.time.Instant;
import java.util.UUID;

record MealTemplateResponse(
        UUID id,
        String name,
        Instant createdAt,
        int itemCount,
        MacroTotals totals
) {}
