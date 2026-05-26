package com.epam.macromind.advice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

interface AiAdviceRepository extends JpaRepository<AiAdvice, UUID> {

    List<AiAdvice> findByUserId(UUID userId);

    List<AiAdvice> findByUserIdAndAdviceTypeOrderByCreatedAtDesc(UUID userId, AdviceType adviceType);

    List<AiAdvice> findByUserIdAndPeriodStart(UUID userId, LocalDate periodStart);

    List<AiAdvice> findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(UUID userId, AdviceType adviceType, LocalDate periodStart);
}
