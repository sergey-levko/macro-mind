package com.epam.macromind.auth;

public record RefreshResponse(String accessToken, String refreshToken) {}
