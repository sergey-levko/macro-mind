package com.epam.macromind.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateUserRequest(
        @NotBlank String name,
        @NotNull Integer age,
        @NotNull BigDecimal weightKg,
        @NotNull BigDecimal heightCm,
        @NotNull GoalType goalType
) {}
