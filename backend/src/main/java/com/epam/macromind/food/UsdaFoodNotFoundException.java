package com.epam.macromind.food;

public class UsdaFoodNotFoundException extends RuntimeException {
    public UsdaFoodNotFoundException(int fdcId) {
        super("USDA food not found: fdcId=" + fdcId);
    }
}
