package com.epam.macromind.dashboard;

import com.epam.macromind.food.Food;
import com.epam.macromind.food.FoodRepository;
import com.epam.macromind.goal.NutritionalGoal;
import com.epam.macromind.goal.NutritionalGoalRepository;
import com.epam.macromind.meal.MealItem;
import com.epam.macromind.meal.MealLog;
import com.epam.macromind.meal.MealType;
import com.epam.macromind.meal.MealLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock MealLogRepository mealLogRepository;
    @Mock FoodRepository foodRepository;
    @Mock NutritionalGoalRepository goalRepository;
    @InjectMocks DashboardService service;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final LocalDate DATE = LocalDate.of(2026, 5, 21);

    @Test
    void getDaily_withMeals_returnsTotals() {
        var food = food(new BigDecimal("200"), new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("5"));
        var item = mealItem(food.getId(), new BigDecimal("100"));
        var log = mealLog(item);
        when(mealLogRepository.findByUserIdAndLoggedAtGreaterThanEqualAndLoggedAtLessThan(eq(USER_ID), any(), any()))
                .thenReturn(List.of(log));
        when(foodRepository.findById(food.getId())).thenReturn(Optional.of(food));
        when(goalRepository.findByUserId(USER_ID)).thenReturn(Optional.of(goal()));

        DailyDashboardResponse result = service.getDailySummary(USER_ID, DATE);

        assertThat(result.totals().caloriesKcal()).isEqualByComparingTo("200.00");
        assertThat(result.totals().proteinG()).isEqualByComparingTo("10.00");
        assertThat(result.targets()).isNotNull();
        assertThat(result.targets().caloriesTarget()).isEqualByComparingTo("2000");
    }

    @Test
    void getDaily_noMeals_returnsZeroTotals() {
        when(mealLogRepository.findByUserIdAndLoggedAtGreaterThanEqualAndLoggedAtLessThan(eq(USER_ID), any(), any()))
                .thenReturn(List.of());
        when(goalRepository.findByUserId(USER_ID)).thenReturn(Optional.of(goal()));

        DailyDashboardResponse result = service.getDailySummary(USER_ID, DATE);

        assertThat(result.totals().caloriesKcal()).isEqualByComparingTo("0");
        assertThat(result.targets()).isNotNull();
    }

    @Test
    void getDaily_noGoal_returnsNullTargets() {
        when(mealLogRepository.findByUserIdAndLoggedAtGreaterThanEqualAndLoggedAtLessThan(eq(USER_ID), any(), any()))
                .thenReturn(List.of());
        when(goalRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        DailyDashboardResponse result = service.getDailySummary(USER_ID, DATE);

        assertThat(result.targets()).isNull();
    }

    @Test
    void getWeekly_partialData_returnsDaysWithZeroAndNonZero() {
        var food = food(new BigDecimal("400"), new BigDecimal("30"), new BigDecimal("40"), new BigDecimal("15"));
        var item = mealItem(food.getId(), new BigDecimal("200"));
        var log = mealLog(item);

        // Only first day has data
        when(mealLogRepository.findByUserIdAndLoggedAtGreaterThanEqualAndLoggedAtLessThan(eq(USER_ID), any(), any()))
                .thenReturn(List.of(log))
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of());
        when(foodRepository.findById(food.getId())).thenReturn(Optional.of(food));
        when(goalRepository.findByUserId(USER_ID)).thenReturn(Optional.of(goal()));

        WeeklyDashboardResponse result = service.getWeeklySummary(USER_ID, DATE);

        assertThat(result.days()).hasSize(7);
        assertThat(result.days().get(0).totals().caloriesKcal()).isEqualByComparingTo("800.00");
        assertThat(result.days().get(1).totals().caloriesKcal()).isEqualByComparingTo("0");
        assertThat(result.weeklyTargets().caloriesTarget()).isEqualByComparingTo("14000");
    }

    @Test
    void getWeekly_noGoal_returnsNullWeeklyTargets() {
        when(mealLogRepository.findByUserIdAndLoggedAtGreaterThanEqualAndLoggedAtLessThan(eq(USER_ID), any(), any()))
                .thenReturn(List.of());
        when(goalRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        WeeklyDashboardResponse result = service.getWeeklySummary(USER_ID, DATE);

        assertThat(result.weeklyTargets()).isNull();
    }

    @Test
    void getSummaryCard_withGoal_returnsPercentages() {
        var food = food(new BigDecimal("200"), new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("5"));
        var item = mealItem(food.getId(), new BigDecimal("100"));
        var log = mealLog(item);
        when(mealLogRepository.findByUserIdAndLoggedAtGreaterThanEqualAndLoggedAtLessThan(eq(USER_ID), any(), any()))
                .thenReturn(List.of(log));
        when(foodRepository.findById(food.getId())).thenReturn(Optional.of(food));
        when(goalRepository.findByUserId(USER_ID)).thenReturn(Optional.of(goal()));

        SummaryDashboardResponse result = service.getSummaryCard(USER_ID);

        assertThat(result.percentages()).isNotNull();
        assertThat(result.percentages().caloriesPct()).isEqualTo(10); // 200/2000 = 10%
    }

    @Test
    void getSummaryCard_noGoal_returnsNullTargetsAndPercentages() {
        when(mealLogRepository.findByUserIdAndLoggedAtGreaterThanEqualAndLoggedAtLessThan(eq(USER_ID), any(), any()))
                .thenReturn(List.of());
        when(goalRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        SummaryDashboardResponse result = service.getSummaryCard(USER_ID);

        assertThat(result.targets()).isNull();
        assertThat(result.percentages()).isNull();
    }

    private NutritionalGoal goal() {
        return new NutritionalGoal(USER_ID,
                new BigDecimal("2000"), new BigDecimal("150"),
                new BigDecimal("200"), new BigDecimal("70"));
    }

    private Food food(BigDecimal cal, BigDecimal protein, BigDecimal carbs, BigDecimal fat) {
        Food f = new Food(USER_ID, "Test Food", "manual", cal, protein, carbs, fat);
        setId(f, UUID.randomUUID());
        return f;
    }

    private MealItem mealItem(UUID foodId, BigDecimal quantityG) {
        MealItem item = new MealItem(null, foodId, quantityG);
        return item;
    }

    private MealLog mealLog(MealItem item) {
        MealLog log = new MealLog(USER_ID, MealType.LUNCH, Instant.now());
        try {
            var field = MealLog.class.getDeclaredField("items");
            field.setAccessible(true);
            ((java.util.List<MealItem>) field.get(log)).add(item);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return log;
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
