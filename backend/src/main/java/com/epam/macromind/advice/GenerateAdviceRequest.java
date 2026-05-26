package com.epam.macromind.advice;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

record GenerateAdviceRequest(
        @NotNull AdviceType adviceType,
        @NotNull LocalDate periodStart,
        boolean preview,
        String content
) {}
