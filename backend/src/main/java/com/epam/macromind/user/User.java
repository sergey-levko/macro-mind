package com.epam.macromind.user;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "users")
class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    private Integer age;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", length = 50)
    private GoalType goalType;

    protected User() {}

    User(String name, String email, Integer age, BigDecimal weightKg, BigDecimal heightCm, GoalType goalType) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.goalType = goalType;
    }

    UUID getId() { return id; }
    String getEmail() { return email; }
    String getName() { return name; }
    Integer getAge() { return age; }
    BigDecimal getWeightKg() { return weightKg; }
    BigDecimal getHeightCm() { return heightCm; }
    GoalType getGoalType() { return goalType; }
}
