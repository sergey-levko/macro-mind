package com.epam.macromind.meal;

import java.math.BigDecimal;

public record MacroTotals(
        BigDecimal caloriesKcal,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG
) {
    public static final MacroTotals ZERO = new MacroTotals(
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

    public MacroTotals add(MacroTotals other) {
        return new MacroTotals(
                caloriesKcal.add(other.caloriesKcal),
                proteinG.add(other.proteinG),
                carbsG.add(other.carbsG),
                fatG.add(other.fatG));
    }
}
