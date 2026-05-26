package com.epam.macromind.advice;

import com.epam.macromind.food.Food;
import com.epam.macromind.food.FoodRepository;
import com.epam.macromind.goal.NutritionalGoal;
import com.epam.macromind.meal.MealItem;
import com.epam.macromind.meal.MealLog;
import com.epam.macromind.meal.MealLogRepository;
import com.epam.macromind.user.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
class AdvicePromptBuilder {

    private final MealLogRepository mealLogRepository;
    private final FoodRepository foodRepository;

    AdvicePromptBuilder(MealLogRepository mealLogRepository, FoodRepository foodRepository) {
        this.mealLogRepository = mealLogRepository;
        this.foodRepository = foodRepository;
    }

    String buildSystemPrompt(User user, NutritionalGoal goal) {
        return """
                You are a personal nutrition coach. Analyze the user's food intake and provide actionable advice.

                User profile:
                - Name: %s
                - Age: %s
                - Weight: %s kg
                - Height: %s cm
                - Goal: %s

                Daily nutritional targets:
                - Calories: %s kcal
                - Protein: %s g
                - Carbs: %s g
                - Fat: %s g

                Provide specific, encouraging advice based on actual intake vs targets.
                """.formatted(
                user.getName(),
                user.getAge() != null ? user.getAge() : "not specified",
                user.getWeightKg() != null ? user.getWeightKg() : "not specified",
                user.getHeightCm() != null ? user.getHeightCm() : "not specified",
                user.getGoalType() != null ? user.getGoalType() : "not specified",
                goal.getCaloriesTarget(),
                goal.getProteinG(),
                goal.getCarbsG(),
                goal.getFatG()
        );
    }

    String buildUserPrompt(UUID userId, AdviceType adviceType, LocalDate periodStart) {
        LocalDate periodEnd = adviceType == AdviceType.DAILY ? periodStart : periodStart.plusDays(6);

        var start = periodStart.atStartOfDay(ZoneOffset.UTC).toInstant();
        var end = periodEnd.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<MealLog> logs = mealLogRepository.findWithItemsByUserIdAndLoggedAtBetween(userId, start, end);

        Map<LocalDate, DailyTotals> dailyMap = new HashMap<>();
        for (MealLog log : logs) {
            LocalDate day = log.getLoggedAt().atZone(ZoneOffset.UTC).toLocalDate();
            DailyTotals totals = dailyMap.computeIfAbsent(day, d -> new DailyTotals());
            for (MealItem item : log.getItems()) {
                Food food = foodRepository.findById(item.getFoodId()).orElse(null);
                if (food == null) continue;
                BigDecimal factor = item.getQuantityG().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                totals.calories = totals.calories.add(safeMultiply(food.getCalories100g(), factor));
                totals.protein = totals.protein.add(safeMultiply(food.getProteinG(), factor));
                totals.carbs = totals.carbs.add(safeMultiply(food.getCarbsG(), factor));
                totals.fat = totals.fat.add(safeMultiply(food.getFatG(), factor));
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(adviceType == AdviceType.DAILY
                ? "Daily intake summary for " + periodStart + ":\n"
                : "Weekly intake summary (" + periodStart + " to " + periodEnd + "):\n");

        if (dailyMap.isEmpty()) {
            sb.append("No meals logged for this period.\n");
        } else {
            periodStart.datesUntil(periodEnd.plusDays(1)).forEach(day -> {
                DailyTotals t = dailyMap.getOrDefault(day, new DailyTotals());
                sb.append(String.format("  %s: %.0f kcal | protein %.1fg | carbs %.1fg | fat %.1fg%n",
                        day, t.calories, t.protein, t.carbs, t.fat));
            });
        }

        sb.append("\nPlease provide personalized nutrition advice for this period.");
        return sb.toString();
    }

    private BigDecimal safeMultiply(BigDecimal value, BigDecimal factor) {
        if (value == null) return BigDecimal.ZERO;
        return value.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private static class DailyTotals {
        BigDecimal calories = BigDecimal.ZERO;
        BigDecimal protein = BigDecimal.ZERO;
        BigDecimal carbs = BigDecimal.ZERO;
        BigDecimal fat = BigDecimal.ZERO;
    }
}
