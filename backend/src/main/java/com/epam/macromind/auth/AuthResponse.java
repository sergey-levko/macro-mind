package com.epam.macromind.auth;

import com.epam.macromind.user.UserResponse;

public record AuthResponse(String token, UserResponse user) {}
