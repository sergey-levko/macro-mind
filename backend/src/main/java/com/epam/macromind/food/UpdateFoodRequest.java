package com.epam.macromind.food;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateFoodRequest(
        @NotBlank String name,
        @NotNull @DecimalMin("0") BigDecimal calories100g,
        @NotNull @DecimalMin("0") BigDecimal proteinG,
        @NotNull @DecimalMin("0") BigDecimal carbsG,
        @NotNull @DecimalMin("0") BigDecimal fatG
) {}
