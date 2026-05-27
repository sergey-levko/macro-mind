package com.epam.macromind.template;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
class NoMealsForDateException extends RuntimeException {
    NoMealsForDateException(String date) {
        super("No meals found for date: " + date);
    }
}
