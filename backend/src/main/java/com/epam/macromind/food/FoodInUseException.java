package com.epam.macromind.food;

import java.util.UUID;

public class FoodInUseException extends RuntimeException {
    public FoodInUseException(UUID foodId) {
        super("Food " + foodId + " is used in meal logs and cannot be deleted.");
    }
}
