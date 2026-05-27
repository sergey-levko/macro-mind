package com.epam.macromind.template;

import com.epam.macromind.meal.MealType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

record ApplyTemplateRequest(
        @NotNull LocalDate date,
        @NotNull MealType mealType,
        @NotEmpty List<@Valid ApplyTemplateItemRequest> items
) {}
