package com.epam.macromind.advice;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ai_advice")
class AiAdvice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "advice_type", nullable = false, length = 50)
    private AdviceType adviceType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AiAdvice() {}

    AiAdvice(UUID userId, AdviceType adviceType, String content, LocalDate periodStart) {
        this.userId = userId;
        this.adviceType = adviceType;
        this.content = content;
        this.periodStart = periodStart;
        this.createdAt = Instant.now();
    }

    UUID getId() { return id; }
    UUID getUserId() { return userId; }
    AdviceType getAdviceType() { return adviceType; }
    String getContent() { return content; }
    LocalDate getPeriodStart() { return periodStart; }
    Instant getCreatedAt() { return createdAt; }
}
