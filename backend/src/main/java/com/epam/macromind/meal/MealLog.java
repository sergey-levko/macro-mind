package com.epam.macromind.meal;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meal_logs")
class MealLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 50)
    private MealType mealType;

    @Column(name = "logged_at", nullable = false)
    private Instant loggedAt;

    @OneToMany(mappedBy = "mealLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealItem> items = new ArrayList<>();

    protected MealLog() {}

    MealLog(UUID userId, MealType mealType, Instant loggedAt) {
        this.userId = userId;
        this.mealType = mealType;
        this.loggedAt = loggedAt;
    }

    UUID getId() { return id; }
    UUID getUserId() { return userId; }
    MealType getMealType() { return mealType; }
    Instant getLoggedAt() { return loggedAt; }
    List<MealItem> getItems() { return items; }
}
