package com.epam.macromind.meal;

import com.epam.macromind.food.Food;
import com.epam.macromind.food.FoodNotFoundException;
import com.epam.macromind.food.FoodRepository;
import com.epam.macromind.user.UserNotFoundException;
import com.epam.macromind.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealServiceTest {

    @Mock MealLogRepository mealLogRepository;
    @Mock UserRepository userRepository;
    @Mock FoodRepository foodRepository;
    @InjectMocks MealService service;

    @Test
    void createMealLog_success() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);
        MealLog saved = mealLog(userId);
        when(mealLogRepository.save(any())).thenReturn(saved);

        MealLogResponse result = service.createMealLog(userId,
                new CreateMealLogRequest(MealType.BREAKFAST, null));

        assertThat(result.mealType()).isEqualTo(MealType.BREAKFAST);
        assertThat(result.userId()).isEqualTo(userId);
    }

    @Test
    void createMealLog_userNotFound_throws404() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> service.createMealLog(userId,
                new CreateMealLogRequest(MealType.LUNCH, null)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getMealLogById_success() {
        MealLog log = mealLog(UUID.randomUUID());
        when(mealLogRepository.findById(log.getId())).thenReturn(Optional.of(log));

        MealLogResponse result = service.getMealLogById(log.getId());

        assertThat(result.id()).isEqualTo(log.getId());
        assertThat(result.items()).isEmpty();
    }

    @Test
    void getMealLogById_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(mealLogRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMealLogById(id))
                .isInstanceOf(MealLogNotFoundException.class);
    }

    @Test
    void getMealLogsByDate_returnsResults() {
        UUID userId = UUID.randomUUID();
        MealLog log = mealLog(userId);
        LocalDate date = LocalDate.of(2024, 1, 15);
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        when(mealLogRepository.findByUserIdAndLoggedAtBetween(userId, start, end))
                .thenReturn(List.of(log));
        when(foodRepository.findAllById(any())).thenReturn(List.of());

        List<MealLogSummaryResponse> results = service.getMealLogsByDate(userId, date);

        assertThat(results).hasSize(1);
    }

    @Test
    void deleteMealLog_success() {
        UUID userId = UUID.randomUUID();
        MealLog log = mealLog(userId);
        when(mealLogRepository.findById(log.getId())).thenReturn(Optional.of(log));

        service.deleteMealLog(userId, log.getId());

        verify(mealLogRepository).delete(log);
    }

    @Test
    void deleteMealLog_notFound_throws404() {
        UUID userId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        when(mealLogRepository.findById(logId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteMealLog(userId, logId))
                .isInstanceOf(MealLogNotFoundException.class);
    }

    @Test
    void deleteMealLog_forbidden_throws403() {
        UUID userId = UUID.randomUUID();
        MealLog log = mealLog(UUID.randomUUID());
        when(mealLogRepository.findById(log.getId())).thenReturn(Optional.of(log));

        assertThatThrownBy(() -> service.deleteMealLog(userId, log.getId()))
                .isInstanceOf(MealLogAccessDeniedException.class);
    }

    @Test
    void addItem_success() {
        UUID userId = UUID.randomUUID();
        MealLog log = mealLog(userId);
        Food food = food();
        when(mealLogRepository.findById(log.getId())).thenReturn(Optional.of(log));
        when(foodRepository.findById(food.getId())).thenReturn(Optional.of(food));
        when(mealLogRepository.save(log)).thenReturn(log);

        MealItemResponse result = service.addItem(userId, log.getId(),
                new AddMealItemRequest(food.getId(), new BigDecimal("150")));

        assertThat(result.foodId()).isEqualTo(food.getId());
        assertThat(result.calories()).isEqualByComparingTo("247.50");
    }

    @Test
    void addItem_logNotFound_throws404() {
        UUID userId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        when(mealLogRepository.findById(logId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addItem(userId, logId,
                new AddMealItemRequest(UUID.randomUUID(), BigDecimal.TEN)))
                .isInstanceOf(MealLogNotFoundException.class);
    }

    @Test
    void addItem_foodNotFound_throws404() {
        UUID userId = UUID.randomUUID();
        MealLog log = mealLog(userId);
        UUID foodId = UUID.randomUUID();
        when(mealLogRepository.findById(log.getId())).thenReturn(Optional.of(log));
        when(foodRepository.findById(foodId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addItem(userId, log.getId(),
                new AddMealItemRequest(foodId, BigDecimal.TEN)))
                .isInstanceOf(FoodNotFoundException.class);
    }

    @Test
    void addItem_forbidden_throws403() {
        UUID userId = UUID.randomUUID();
        MealLog log = mealLog(UUID.randomUUID());
        when(mealLogRepository.findById(log.getId())).thenReturn(Optional.of(log));

        assertThatThrownBy(() -> service.addItem(userId, log.getId(),
                new AddMealItemRequest(UUID.randomUUID(), BigDecimal.TEN)))
                .isInstanceOf(MealLogAccessDeniedException.class);
    }

    @Test
    void removeItem_success() {
        UUID userId = UUID.randomUUID();
        MealLog log = mealLog(userId);
        MealItem item = new MealItem(log, UUID.randomUUID(), BigDecimal.TEN);
        setId(item, UUID.randomUUID());
        log.getItems().add(item);
        when(mealLogRepository.findById(log.getId())).thenReturn(Optional.of(log));

        service.removeItem(userId, log.getId(), item.getId());

        assertThat(log.getItems()).isEmpty();
        verify(mealLogRepository).save(log);
    }

    @Test
    void removeItem_notFound_throws404() {
        UUID userId = UUID.randomUUID();
        MealLog log = mealLog(userId);
        when(mealLogRepository.findById(log.getId())).thenReturn(Optional.of(log));

        assertThatThrownBy(() -> service.removeItem(userId, log.getId(), UUID.randomUUID()))
                .isInstanceOf(MealItemNotFoundException.class);
    }

    @Test
    void removeItem_forbidden_throws403() {
        UUID userId = UUID.randomUUID();
        MealLog log = mealLog(UUID.randomUUID());
        when(mealLogRepository.findById(log.getId())).thenReturn(Optional.of(log));

        assertThatThrownBy(() -> service.removeItem(userId, log.getId(), UUID.randomUUID()))
                .isInstanceOf(MealLogAccessDeniedException.class);
    }

    private MealLog mealLog(UUID userId) {
        MealLog log = new MealLog(userId, MealType.BREAKFAST, Instant.now());
        setId(log, UUID.randomUUID());
        return log;
    }

    private Food food() {
        Food food = new Food(UUID.randomUUID(), "Chicken", "CUSTOM",
                new BigDecimal("165"), new BigDecimal("31"), BigDecimal.ZERO, new BigDecimal("3.6"));
        setId(food, UUID.randomUUID());
        return food;
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
