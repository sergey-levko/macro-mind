package com.epam.macromind.user;

import java.math.BigDecimal;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        Integer age,
        BigDecimal weightKg,
        BigDecimal heightCm,
        GoalType goalType
) {
    static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(),
                u.getAge(), u.getWeightKg(), u.getHeightCm(), u.getGoalType());
    }
}
