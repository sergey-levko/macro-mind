package com.epam.macromind.advice;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

record AiAdviceResponse(
        UUID id,
        UUID userId,
        AdviceType adviceType,
        LocalDate periodStart,
        String content,
        Instant createdAt
) {}
