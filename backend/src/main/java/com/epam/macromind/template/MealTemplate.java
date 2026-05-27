package com.epam.macromind.template;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meal_templates")
class MealTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealTemplateItem> items = new ArrayList<>();

    protected MealTemplate() {}

    MealTemplate(UUID userId, String name) {
        this.userId = userId;
        this.name = name;
        this.createdAt = Instant.now();
    }

    UUID getId() { return id; }
    UUID getUserId() { return userId; }
    String getName() { return name; }
    Instant getCreatedAt() { return createdAt; }
    List<MealTemplateItem> getItems() { return items; }
}
