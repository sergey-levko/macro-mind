package com.epam.macromind.advice;

import java.util.UUID;

public class NoGoalForAdviceException extends RuntimeException {
    public NoGoalForAdviceException(UUID userId) {
        super("No nutritional goal set for user: " + userId);
    }
}
