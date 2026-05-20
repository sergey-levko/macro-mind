package com.epam.macromind.food;

import java.util.UUID;

public class FoodAccessDeniedException extends RuntimeException {
    public FoodAccessDeniedException(UUID foodId) {
        super("Access denied for food: " + foodId);
    }
}
