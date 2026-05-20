package com.epam.macromind.goal;

import com.epam.macromind.user.UserNotFoundException;
import com.epam.macromind.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionalGoalServiceTest {

    @Mock NutritionalGoalRepository goalRepository;
    @Mock UserRepository userRepository;
    @InjectMocks NutritionalGoalService service;

    private static final SetNutritionalGoalRequest SAMPLE_REQUEST = new SetNutritionalGoalRequest(
            new BigDecimal("2000"), new BigDecimal("150"),
            new BigDecimal("200"), new BigDecimal("70"));

    @Test
    void setGoal_newGoal_success() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);
        when(goalRepository.findByUserId(userId)).thenReturn(Optional.empty());
        NutritionalGoal saved = goal(userId);
        when(goalRepository.save(any())).thenReturn(saved);

        NutritionalGoalResponse result = service.setGoal(userId, SAMPLE_REQUEST);

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.caloriesTarget()).isEqualByComparingTo("2000");
        verify(goalRepository, never()).delete(any());
    }

    @Test
    void setGoal_replacesExistingGoal() {
        UUID userId = UUID.randomUUID();
        NutritionalGoal existing = goal(userId);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(goalRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        NutritionalGoal saved = goal(userId);
        when(goalRepository.save(any())).thenReturn(saved);

        service.setGoal(userId, SAMPLE_REQUEST);

        verify(goalRepository).delete(existing);
        verify(goalRepository).save(any());
    }

    @Test
    void setGoal_userNotFound_throws404() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> service.setGoal(userId, SAMPLE_REQUEST))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getGoal_success() {
        UUID userId = UUID.randomUUID();
        NutritionalGoal existing = goal(userId);
        when(goalRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        NutritionalGoalResponse result = service.getGoal(userId);

        assertThat(result.userId()).isEqualTo(userId);
    }

    @Test
    void getGoal_notFound_throws404() {
        UUID userId = UUID.randomUUID();
        when(goalRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getGoal(userId))
                .isInstanceOf(GoalNotFoundException.class);
    }

    @Test
    void deleteGoal_success() {
        UUID userId = UUID.randomUUID();
        NutritionalGoal existing = goal(userId);
        when(goalRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        service.deleteGoal(userId);

        verify(goalRepository).delete(existing);
    }

    @Test
    void deleteGoal_notFound_throws404() {
        UUID userId = UUID.randomUUID();
        when(goalRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteGoal(userId))
                .isInstanceOf(GoalNotFoundException.class);
    }

    private NutritionalGoal goal(UUID userId) {
        NutritionalGoal g = new NutritionalGoal(userId,
                new BigDecimal("2000"), new BigDecimal("150"),
                new BigDecimal("200"), new BigDecimal("70"));
        setId(g, UUID.randomUUID());
        return g;
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
