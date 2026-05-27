package com.epam.macromind.template;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
class MealTemplateNotFoundException extends RuntimeException {
    MealTemplateNotFoundException(UUID id) {
        super("Meal template not found: " + id);
    }
}
