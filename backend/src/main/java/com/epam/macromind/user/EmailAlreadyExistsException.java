package com.epam.macromind.user;

class EmailAlreadyExistsException extends RuntimeException {
    EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email);
    }
}
