package com.epam.macromind.auth;

import java.util.UUID;

record RotationResult(UUID userId, String rawRefreshToken) {}
