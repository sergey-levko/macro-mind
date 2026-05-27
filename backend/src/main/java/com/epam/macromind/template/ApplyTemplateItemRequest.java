package com.epam.macromind.template;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

record ApplyTemplateItemRequest(
        @NotNull UUID foodId,
        @NotNull @DecimalMin("0.1") BigDecimal quantityG
) {}
