package com.epam.macromind.meal;

import java.util.UUID;

public class MealLogNotFoundException extends RuntimeException {
    public MealLogNotFoundException(UUID id) {
        super("Meal log not found: " + id);
    }
}
