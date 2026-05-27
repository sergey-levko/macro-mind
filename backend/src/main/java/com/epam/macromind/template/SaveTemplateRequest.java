package com.epam.macromind.template;

import com.epam.macromind.meal.MealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

record SaveTemplateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull LocalDate date,
        @NotNull MealType mealType
) {}
