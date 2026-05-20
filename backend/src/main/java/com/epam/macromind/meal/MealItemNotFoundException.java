package com.epam.macromind.meal;

import java.util.UUID;

public class MealItemNotFoundException extends RuntimeException {
    public MealItemNotFoundException(UUID id) {
        super("Meal item not found: " + id);
    }
}
