package com.epam.macromind.template;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MealTemplateResponse(
        UUID id,
        String name,
        Instant createdAt,
        List<MealTemplateItemResponse> items
) {}
