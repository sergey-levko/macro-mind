package com.epam.macromind.meal;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record UpdateMealLogRequest(
        @NotNull Instant loggedAt
) {}
