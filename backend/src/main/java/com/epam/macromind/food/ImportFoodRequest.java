package com.epam.macromind.food;

import jakarta.validation.constraints.NotNull;

record ImportFoodRequest(@NotNull Integer fdcId) {}
