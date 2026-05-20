package com.epam.macromind.food;

import java.util.UUID;

public class FoodNotFoundException extends RuntimeException {
    public FoodNotFoundException(UUID id) {
        super("Food not found: " + id);
    }
}
