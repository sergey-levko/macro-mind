package com.epam.macromind.user;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    UserNotFoundException(UUID id) {
        super("User not found: " + id);
    }
}
