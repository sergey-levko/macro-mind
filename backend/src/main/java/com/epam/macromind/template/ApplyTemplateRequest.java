package com.epam.macromind.template;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

record ApplyTemplateRequest(@NotNull LocalDate date) {}
