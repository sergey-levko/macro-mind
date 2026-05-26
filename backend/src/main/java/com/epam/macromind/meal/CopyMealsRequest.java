package com.epam.macromind.meal;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CopyMealsRequest(
        @NotNull LocalDate date
) {}
