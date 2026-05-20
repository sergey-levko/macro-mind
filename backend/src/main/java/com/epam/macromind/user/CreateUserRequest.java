package com.epam.macromind.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateUserRequest(
        @NotBlank String name,
        @NotBlank String email,
        @NotNull Integer age,
        @NotNull BigDecimal weightKg,
        @NotNull BigDecimal heightCm,
        @NotNull GoalType goalType
) {}
