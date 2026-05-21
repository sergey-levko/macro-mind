package com.epam.macromind.goal;

import java.math.BigDecimal;

record GoalSuggestionResponse(
        BigDecimal caloriesTarget,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG) {}
