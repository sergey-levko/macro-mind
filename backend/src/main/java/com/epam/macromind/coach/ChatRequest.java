package com.epam.macromind.coach;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank String message) {}
