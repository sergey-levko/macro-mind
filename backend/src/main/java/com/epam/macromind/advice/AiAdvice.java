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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdviceStatus status;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AiAdvice() {}

    AiAdvice(UUID userId, AdviceType adviceType, LocalDate periodStart) {
        this.userId = userId;
        this.adviceType = adviceType;
        this.content = "";
        this.status = AdviceStatus.PENDING;
        this.periodStart = periodStart;
        this.createdAt = Instant.now();
    }

    AiAdvice(UUID userId, AdviceType adviceType, String content, LocalDate periodStart) {
        this.userId = userId;
        this.adviceType = adviceType;
        this.content = content;
        this.status = AdviceStatus.COMPLETED;
        this.periodStart = periodStart;
        this.createdAt = Instant.now();
    }

    void complete(String content) {
        this.content = content;
        this.status = AdviceStatus.COMPLETED;
    }

    void fail() {
        this.status = AdviceStatus.FAILED;
    }

    UUID getId() { return id; }
    UUID getUserId() { return userId; }
    AdviceType getAdviceType() { return adviceType; }
    String getContent() { return content; }
    AdviceStatus getStatus() { return status; }
    LocalDate getPeriodStart() { return periodStart; }
    Instant getCreatedAt() { return createdAt; }
}
