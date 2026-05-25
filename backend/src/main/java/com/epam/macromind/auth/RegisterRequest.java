package com.epam.macromind.auth;

import com.epam.macromind.user.GoalType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotNull Integer age,
        @NotNull BigDecimal weightKg,
        @NotNull BigDecimal heightCm,
        @NotNull GoalType goalType
) {}
