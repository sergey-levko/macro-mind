package com.epam.macromind.settings;

import jakarta.validation.constraints.NotNull;

public record UpdateUserSettingsRequest(@NotNull Boolean usdaEnabled) {}
