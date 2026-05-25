package com.epam.macromind.user;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

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

    @Column(name = "password_hash", nullable = false)
    private String passwordHash = "";

    protected User() {}

    public User(String name, String email, String passwordHash, Integer age, BigDecimal weightKg, BigDecimal heightCm, GoalType goalType) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.age = age;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.goalType = goalType;
    }

    void update(String name, Integer age, BigDecimal weightKg, BigDecimal heightCm, GoalType goalType) {
        this.name = name;
        this.age = age;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.goalType = goalType;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public Integer getAge() { return age; }
    public BigDecimal getWeightKg() { return weightKg; }
    public BigDecimal getHeightCm() { return heightCm; }
    public GoalType getGoalType() { return goalType; }
    public String getPasswordHash() { return passwordHash; }
}
