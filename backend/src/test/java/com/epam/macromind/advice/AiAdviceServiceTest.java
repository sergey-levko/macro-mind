package com.epam.macromind.advice;

import com.epam.macromind.goal.NutritionalGoal;
import com.epam.macromind.goal.NutritionalGoalRepository;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAdviceServiceTest {

    @Mock UserRepository userRepository;
    @Mock NutritionalGoalRepository goalRepository;
    @Mock AiAdviceRepository adviceRepository;
    @Mock AdvicePromptBuilder promptBuilder;
    @Mock ChatClient chatClient;
    @Mock ChatClient.ChatClientRequestSpec requestSpec;
    @Mock ChatClient.CallResponseSpec callResponseSpec;
    @Mock AsyncAdviceGenerator asyncAdviceGenerator;

    AiAdviceService service;

    private final UUID userId = UUID.randomUUID();
    private final LocalDate today = LocalDate.of(2026, 5, 20);
    private final GenerateAdviceRequest dailyRequest = new GenerateAdviceRequest(AdviceType.DAILY, today, false, null);

    @BeforeEach
    void setUp() {
        service = new AiAdviceService(chatClient, userRepository, goalRepository, adviceRepository, promptBuilder, asyncAdviceGenerator);
    }

    @Test
    void generateAdvice_success() {
        var user = sampleUser();
        var goal = sampleGoal();
        var saved = sampleAdvice();
        when(adviceRepository.findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(userId, AdviceType.DAILY, today))
                .thenReturn(List.of());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(goalRepository.findByUserId(userId)).thenReturn(Optional.of(goal));
        when(promptBuilder.buildSystemPrompt(user, goal)).thenReturn("sys");
        when(promptBuilder.buildUserPrompt(userId, AdviceType.DAILY, today)).thenReturn("usr");
        when(adviceRepository.save(any())).thenReturn(saved);

        GenerateAdviceResult result = service.generateAdvice(userId, dailyRequest);

        assertThat(result.response().userId()).isEqualTo(userId);
        assertThat(result.response().adviceType()).isEqualTo(AdviceType.DAILY);
        assertThat(result.response().status()).isEqualTo(AdviceStatus.PENDING);
        assertThat(result.created()).isTrue();
        verify(adviceRepository).save(any());
        verify(asyncAdviceGenerator).complete(any(), eq("sys"), eq("usr"));
        verifyNoInteractions(chatClient);
    }

    @Test
    void generateAdvice_duplicateNonPreview_returnsExistingWithoutCallingAI() {
        var existing = sampleCompletedAdvice();
        when(adviceRepository.findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(userId, AdviceType.DAILY, today))
                .thenReturn(List.of(existing));

        GenerateAdviceResult result = service.generateAdvice(userId, dailyRequest);

        assertThat(result.created()).isFalse();
        assertThat(result.response().id()).isEqualTo(existing.getId());
        verifyNoInteractions(chatClient, userRepository, goalRepository, asyncAdviceGenerator);
        verify(adviceRepository, never()).save(any());
    }

    @Test
    void generateAdvice_duplicatePreview_callsAIWithoutSaving() {
        var previewRequest = new GenerateAdviceRequest(AdviceType.DAILY, today, true, null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser()));
        when(goalRepository.findByUserId(userId)).thenReturn(Optional.of(sampleGoal()));
        when(promptBuilder.buildSystemPrompt(any(), any())).thenReturn("sys");
        when(promptBuilder.buildUserPrompt(userId, AdviceType.DAILY, today)).thenReturn("usr");
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system("sys")).thenReturn(requestSpec);
        when(requestSpec.user("usr")).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Fresh preview.");

        GenerateAdviceResult result = service.generateAdvice(userId, previewRequest);

        assertThat(result.created()).isFalse();
        assertThat(result.response().content()).isEqualTo("Fresh preview.");
        assertThat(result.response().status()).isEqualTo(AdviceStatus.COMPLETED);
        assertThat(result.response().id()).isNull();
        verify(adviceRepository, never()).save(any());
        verify(adviceRepository, never()).findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(any(), any(), any());
        verifyNoInteractions(asyncAdviceGenerator);
    }

    @Test
    void generateAdvice_withContent_savesDirectlyAsCompleted() {
        var contentRequest = new GenerateAdviceRequest(AdviceType.DAILY, today, false, "Saved preview content.");
        var user = sampleUser();
        var goal = sampleGoal();
        var saved = sampleCompletedAdvice();
        when(adviceRepository.findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(userId, AdviceType.DAILY, today))
                .thenReturn(List.of());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(goalRepository.findByUserId(userId)).thenReturn(Optional.of(goal));
        when(adviceRepository.save(any())).thenReturn(saved);

        GenerateAdviceResult result = service.generateAdvice(userId, contentRequest);

        assertThat(result.created()).isTrue();
        verify(adviceRepository).save(any());
        verifyNoInteractions(asyncAdviceGenerator, chatClient);
    }

    @Test
    void generateAdvice_userNotFound_throws404() {
        when(adviceRepository.findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(userId, AdviceType.DAILY, today))
                .thenReturn(List.of());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateAdvice(userId, dailyRequest))
                .isInstanceOf(UserNotFoundException.class);
        verify(adviceRepository, never()).save(any());
    }

    @Test
    void generateAdvice_noGoal_throws400() {
        when(adviceRepository.findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(userId, AdviceType.DAILY, today))
                .thenReturn(List.of());
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser()));
        when(goalRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateAdvice(userId, dailyRequest))
                .isInstanceOf(NoGoalForAdviceException.class);
        verify(adviceRepository, never()).save(any());
    }

    @Test
    void getAdvice_success() {
        var advice = sampleCompletedAdvice();
        when(adviceRepository.findById(advice.getId())).thenReturn(Optional.of(advice));

        AiAdviceResponse result = service.getAdvice(advice.getId());

        assertThat(result.id()).isEqualTo(advice.getId());
    }

    @Test
    void getAdvice_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(adviceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAdvice(id))
                .isInstanceOf(AdviceNotFoundException.class);
    }

    @Test
    void deleteAdvice_success() {
        var advice = sampleCompletedAdvice();
        when(adviceRepository.findById(advice.getId())).thenReturn(Optional.of(advice));

        service.deleteAdvice(userId, advice.getId());

        verify(adviceRepository).deleteById(advice.getId());
    }

    @Test
    void deleteAdvice_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(adviceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteAdvice(userId, id))
                .isInstanceOf(AdviceNotFoundException.class);
        verify(adviceRepository, never()).deleteById(any());
    }

    @Test
    void deleteAdvice_wrongOwner_throws404() {
        var advice = sampleCompletedAdvice();
        UUID otherId = UUID.randomUUID();
        when(adviceRepository.findById(advice.getId())).thenReturn(Optional.of(advice));

        assertThatThrownBy(() -> service.deleteAdvice(otherId, advice.getId()))
                .isInstanceOf(AdviceNotFoundException.class);
        verify(adviceRepository, never()).deleteById(any());
    }

    @Test
    void listAdvice_noFilters_returnsAll() {
        when(adviceRepository.findByUserId(userId)).thenReturn(List.of(sampleCompletedAdvice()));

        List<AiAdviceResponse> results = service.listAdvice(userId, null, null);

        assertThat(results).hasSize(1);
        verify(adviceRepository).findByUserId(userId);
    }

    @Test
    void listAdvice_withAdviceTypeFilter() {
        when(adviceRepository.findByUserIdAndAdviceType(userId, AdviceType.DAILY))
                .thenReturn(List.of(sampleCompletedAdvice()));

        List<AiAdviceResponse> results = service.listAdvice(userId, AdviceType.DAILY, null);

        assertThat(results).hasSize(1);
        verify(adviceRepository).findByUserIdAndAdviceType(userId, AdviceType.DAILY);
    }

    @Test
    void listAdvice_withAllFilters() {
        when(adviceRepository.findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(userId, AdviceType.DAILY, today))
                .thenReturn(List.of(sampleCompletedAdvice()));

        List<AiAdviceResponse> results = service.listAdvice(userId, AdviceType.DAILY, today);

        assertThat(results).hasSize(1);
        verify(adviceRepository).findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(userId, AdviceType.DAILY, today);
    }

    private User sampleUser() {
        return new User("Alice", "alice@example.com", "", 30,
                new BigDecimal("65"), new BigDecimal("170"), null);
    }

    private NutritionalGoal sampleGoal() {
        return new NutritionalGoal(userId, new BigDecimal("2000"),
                new BigDecimal("150"), new BigDecimal("200"), new BigDecimal("70"));
    }

    private AiAdvice sampleAdvice() {
        AiAdvice a = new AiAdvice(userId, AdviceType.DAILY, today);
        setId(a, UUID.randomUUID());
        return a;
    }

    private AiAdvice sampleCompletedAdvice() {
        AiAdvice a = new AiAdvice(userId, AdviceType.DAILY, "Eat more protein.", today);
        setId(a, UUID.randomUUID());
        return a;
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
