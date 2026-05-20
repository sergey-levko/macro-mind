package com.epam.macromind.goal;

import java.util.UUID;

public class GoalNotFoundException extends RuntimeException {

    GoalNotFoundException(UUID userId) {
        super("No nutritional goal found for user " + userId);
    }
}
