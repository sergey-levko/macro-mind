package com.epam.macromind.dashboard;

import java.math.BigDecimal;

record MacroTotals(
        BigDecimal caloriesKcal,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG
) {
    static MacroTotals zero() {
        return new MacroTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    MacroTotals add(MacroTotals other) {
        return new MacroTotals(
                caloriesKcal.add(other.caloriesKcal),
                proteinG.add(other.proteinG),
                carbsG.add(other.carbsG),
                fatG.add(other.fatG)
        );
    }

    MacroTotals multiplyBy(int factor) {
        BigDecimal f = BigDecimal.valueOf(factor);
        return new MacroTotals(
                caloriesKcal.multiply(f),
                proteinG.multiply(f),
                carbsG.multiply(f),
                fatG.multiply(f)
        );
    }
}
