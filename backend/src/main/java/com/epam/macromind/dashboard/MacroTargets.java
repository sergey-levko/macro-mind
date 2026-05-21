package com.epam.macromind.dashboard;

import java.math.BigDecimal;

record MacroTargets(
        BigDecimal caloriesTarget,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG
) {
    MacroTargets multiplyBy(int factor) {
        BigDecimal f = BigDecimal.valueOf(factor);
        return new MacroTargets(
                caloriesTarget.multiply(f),
                proteinG.multiply(f),
                carbsG.multiply(f),
                fatG.multiply(f)
        );
    }
}
