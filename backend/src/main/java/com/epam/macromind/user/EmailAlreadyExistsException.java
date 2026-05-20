package com.epam.macromind.user;

public class EmailAlreadyExistsException extends RuntimeException {
    EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email);
    }
}
