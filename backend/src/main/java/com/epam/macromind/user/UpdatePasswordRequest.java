package com.epam.macromind.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record UpdatePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8, message = "must be at least 8 characters") String newPassword
) {}
