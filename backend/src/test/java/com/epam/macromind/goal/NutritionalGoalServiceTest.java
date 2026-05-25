package com.epam.macromind.goal;

import com.epam.macromind.user.GoalType;
import com.epam.macromind.user.User;
import com.epam.macromind.user.UserNotFoundException;
import com.epam.macromind.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

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
    @Mock ChatClient chatClient;
    @Mock ChatClient.ChatClientRequestSpec requestSpec;
    @Mock ChatClient.CallResponseSpec callResponseSpec;

    NutritionalGoalService service;

    @BeforeEach
    void setUp() {
        service = new NutritionalGoalService(goalRepository, userRepository, chatClient);
    }

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
    void setGoal_updatesExistingGoal() {
        UUID userId = UUID.randomUUID();
        NutritionalGoal existing = goal(userId);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(goalRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        NutritionalGoalResponse result = service.setGoal(userId, SAMPLE_REQUEST);

        assertThat(result.caloriesTarget()).isEqualByComparingTo("2000");
        verify(goalRepository, never()).delete(any());
        verify(goalRepository, never()).save(any());
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

    @Test
    void generateGoal_success_returnssuggestion() {
        UUID userId = UUID.randomUUID();
        User user = new User("Alice", "alice@example.com", "", 30,
                new BigDecimal("70"), new BigDecimal("175"), GoalType.LOSE_WEIGHT);
        GoalSuggestionResponse suggestion = new GoalSuggestionResponse(
                new BigDecimal("1800"), new BigDecimal("140"),
                new BigDecimal("180"), new BigDecimal("60"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(GoalSuggestionResponse.class)).thenReturn(suggestion);

        GoalSuggestionResponse result = service.generateGoal(userId);

        assertThat(result.caloriesTarget()).isEqualByComparingTo("1800");
        assertThat(result.proteinG()).isEqualByComparingTo("140");
        verify(chatClient).prompt();
    }

    @Test
    void generateGoal_promptContainsProfileFields() {
        UUID userId = UUID.randomUUID();
        User user = new User("Bob", "bob@example.com", "", 25,
                new BigDecimal("80"), new BigDecimal("180"), GoalType.GAIN_MUSCLE);
        GoalSuggestionResponse suggestion = new GoalSuggestionResponse(
                new BigDecimal("2500"), new BigDecimal("200"),
                new BigDecimal("250"), new BigDecimal("80"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(GoalSuggestionResponse.class)).thenReturn(suggestion);

        service.generateGoal(userId);

        var promptCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(requestSpec).user(promptCaptor.capture());
        String prompt = promptCaptor.getValue();
        assertThat(prompt).contains("GAIN_MUSCLE", "25", "80", "180");
    }

    @Test
    void generateGoal_aiParseFailure_throwsGoalGenerationException() {
        UUID userId = UUID.randomUUID();
        User user = new User("Carol", "carol@example.com", "", 28,
                new BigDecimal("65"), new BigDecimal("165"), GoalType.MAINTAIN_WEIGHT);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(GoalSuggestionResponse.class))
                .thenThrow(new RuntimeException("JSON parse error"));

        assertThatThrownBy(() -> service.generateGoal(userId))
                .isInstanceOf(GoalGenerationException.class)
                .hasMessageContaining("Failed to generate");
    }

    @Test
    void generateGoal_userNotFound_throwsUserNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateGoal(userId))
                .isInstanceOf(UserNotFoundException.class);
        verifyNoInteractions(chatClient);
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
