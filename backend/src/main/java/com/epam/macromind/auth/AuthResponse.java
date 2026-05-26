package com.epam.macromind.auth;

import com.epam.macromind.user.UserResponse;

public record AuthResponse(String accessToken, String refreshToken, UserResponse user) {}
