package com.epam.macromind.dashboard;

import com.epam.macromind.food.FoodRepository;
import com.epam.macromind.goal.NutritionalGoal;
import com.epam.macromind.goal.NutritionalGoalRepository;
import com.epam.macromind.meal.MealLog;
import com.epam.macromind.meal.MealLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
class DashboardService {

    private static final int PCT_CAP = 200;

    private final MealLogRepository mealLogRepository;
    private final FoodRepository foodRepository;
    private final NutritionalGoalRepository goalRepository;

    DashboardService(MealLogRepository mealLogRepository,
                     FoodRepository foodRepository,
                     NutritionalGoalRepository goalRepository) {
        this.mealLogRepository = mealLogRepository;
        this.foodRepository = foodRepository;
        this.goalRepository = goalRepository;
    }

    DailyDashboardResponse getDailySummary(UUID userId, LocalDate date) {
        MacroTotals totals = aggregateDay(userId, date);
        MacroTargets targets = goalRepository.findByUserId(userId)
                .map(this::toTargets)
                .orElse(null);
        return new DailyDashboardResponse(date, totals, targets);
    }

    WeeklyDashboardResponse getWeeklySummary(UUID userId, LocalDate weekStart) {
        List<DailyEntry> days = new ArrayList<>();
        MacroTotals weeklyTotals = MacroTotals.zero();

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            MacroTotals dayTotals = aggregateDay(userId, day);
            days.add(new DailyEntry(day, dayTotals));
            weeklyTotals = weeklyTotals.add(dayTotals);
        }

        MacroTargets weeklyTargets = goalRepository.findByUserId(userId)
                .map(g -> toTargets(g).multiplyBy(7))
                .orElse(null);

        return new WeeklyDashboardResponse(weekStart, days, weeklyTotals, weeklyTargets);
    }

    SummaryDashboardResponse getSummaryCard(UUID userId) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        MacroTotals totals = aggregateDay(userId, today);

        MacroTargets targets = goalRepository.findByUserId(userId)
                .map(this::toTargets)
                .orElse(null);

        MacroPercentages percentages = targets == null ? null : computePercentages(totals, targets);
        return new SummaryDashboardResponse(today, totals, targets, percentages);
    }

    private MacroTotals aggregateDay(UUID userId, LocalDate date) {
        var start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        var end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<MealLog> logs = mealLogRepository.findByUserIdAndLoggedAtGreaterThanEqualAndLoggedAtLessThan(userId, start, end);

        MacroTotals totals = MacroTotals.zero();
        for (MealLog log : logs) {
            for (var item : log.getItems()) {
                var food = foodRepository.findById(item.getFoodId()).orElse(null);
                if (food == null) continue;
                BigDecimal factor = item.getQuantityG().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                totals = totals.add(new MacroTotals(
                        safe(food.getCalories100g(), factor),
                        safe(food.getProteinG(), factor),
                        safe(food.getCarbsG(), factor),
                        safe(food.getFatG(), factor)
                ));
            }
        }
        return totals;
    }

    private BigDecimal safe(BigDecimal value, BigDecimal factor) {
        if (value == null) return BigDecimal.ZERO;
        return value.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private MacroTargets toTargets(NutritionalGoal goal) {
        return new MacroTargets(
                goal.getCaloriesTarget(),
                goal.getProteinG(),
                goal.getCarbsG(),
                goal.getFatG()
        );
    }

    private MacroPercentages computePercentages(MacroTotals totals, MacroTargets targets) {
        return new MacroPercentages(
                pct(totals.caloriesKcal(), targets.caloriesTarget()),
                pct(totals.proteinG(), targets.proteinG()),
                pct(totals.carbsG(), targets.carbsG()),
                pct(totals.fatG(), targets.fatG())
        );
    }

    private int pct(BigDecimal actual, BigDecimal target) {
        if (target == null || target.compareTo(BigDecimal.ZERO) == 0) return 0;
        int raw = actual.divide(target, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .intValue();
        return Math.min(raw, PCT_CAP);
    }
}
