package com.epam.macromind.meal;

import java.math.BigDecimal;

record MacroTotals(
        BigDecimal calories,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG
) {
    static final MacroTotals ZERO = new MacroTotals(
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

    MacroTotals add(MacroTotals other) {
        return new MacroTotals(
                calories.add(other.calories),
                proteinG.add(other.proteinG),
                carbsG.add(other.carbsG),
                fatG.add(other.fatG));
    }
}
