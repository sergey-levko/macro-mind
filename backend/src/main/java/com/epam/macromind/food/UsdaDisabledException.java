package com.epam.macromind.food;

public class UsdaDisabledException extends RuntimeException {
    public UsdaDisabledException() {
        super("USDA integration is disabled for this user");
    }
}
