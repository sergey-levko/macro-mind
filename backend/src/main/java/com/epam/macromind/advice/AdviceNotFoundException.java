package com.epam.macromind.advice;

import java.util.UUID;

public class AdviceNotFoundException extends RuntimeException {
    public AdviceNotFoundException(UUID id) {
        super("Advice not found: " + id);
    }
}
