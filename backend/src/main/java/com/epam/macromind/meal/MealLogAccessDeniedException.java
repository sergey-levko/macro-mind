package com.epam.macromind.meal;

import java.util.UUID;

public class MealLogAccessDeniedException extends RuntimeException {
    public MealLogAccessDeniedException(UUID id) {
        super("Access denied for meal log: " + id);
    }
}
