package com.epam.macromind.advice;

import com.epam.macromind.meal.MealLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsightSchedulerTest {

    @Mock MealLogRepository mealLogRepository;
    @Mock AiAdviceRepository adviceRepository;
    @Mock AiAdviceService adviceService;

    InsightScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new InsightScheduler(mealLogRepository, adviceRepository, adviceService);
    }

    // ── Daily job ────────────────────────────────────────────────────────────

    @Test
    void dailyJob_skipsUserWithExistingInsight() {
        UUID userId = UUID.randomUUID();
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);

        when(mealLogRepository.findDistinctUserIdsByLoggedAtBetween(any(), any()))
                .thenReturn(List.of(userId));
        when(adviceRepository.findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(
                userId, AdviceType.DAILY, yesterday))
                .thenReturn(List.of(mock(AiAdvice.class)));

        scheduler.generateDailyInsights();

        verify(adviceService, never()).generateAdvice(any(), any());
    }

    @Test
    void dailyJob_skipsUserWithNoMeals() {
        when(mealLogRepository.findDistinctUserIdsByLoggedAtBetween(any(), any()))
                .thenReturn(List.of());

        scheduler.generateDailyInsights();

        verify(adviceRepository, never()).findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(any(), any(), any());
        verify(adviceService, never()).generateAdvice(any(), any());
    }

    @Test
    void dailyJob_generatesInsightForEligibleUser() {
        UUID userId = UUID.randomUUID();
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);

        when(mealLogRepository.findDistinctUserIdsByLoggedAtBetween(any(), any()))
                .thenReturn(List.of(userId));
        when(adviceRepository.findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(
                userId, AdviceType.DAILY, yesterday))
                .thenReturn(List.of());

        scheduler.generateDailyInsights();

        ArgumentCaptor<GenerateAdviceRequest> captor = ArgumentCaptor.forClass(GenerateAdviceRequest.class);
        verify(adviceService).generateAdvice(eq(userId), captor.capture());
        assertThat(captor.getValue().adviceType()).isEqualTo(AdviceType.DAILY);
        assertThat(captor.getValue().periodStart()).isEqualTo(yesterday);
        assertThat(captor.getValue().preview()).isFalse();
    }

    @Test
    void dailyJob_isolatesPerUserFailure() {
        UUID failingUser = UUID.randomUUID();
        UUID successUser = UUID.randomUUID();
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);

        when(mealLogRepository.findDistinctUserIdsByLoggedAtBetween(any(), any()))
                .thenReturn(List.of(failingUser, successUser));
        when(adviceRepository.findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(any(), eq(AdviceType.DAILY), eq(yesterday)))
                .thenReturn(List.of());
        doThrow(new RuntimeException("Claude unavailable"))
                .when(adviceService).generateAdvice(eq(failingUser), any());

        scheduler.generateDailyInsights();

        verify(adviceService).generateAdvice(eq(successUser), any());
    }

    // ── Weekly job ───────────────────────────────────────────────────────────

    @Test
    void weeklyJob_skipsUserWithExistingInsight() {
        UUID userId = UUID.randomUUID();
        LocalDate monday = LocalDate.now(ZoneOffset.UTC).with(DayOfWeek.MONDAY);

        when(mealLogRepository.findDistinctUserIdsByLoggedAtBetween(any(), any()))
                .thenReturn(List.of(userId));
        when(adviceRepository.findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(
                userId, AdviceType.WEEKLY, monday))
                .thenReturn(List.of(mock(AiAdvice.class)));

        scheduler.generateWeeklyInsights();

        verify(adviceService, never()).generateAdvice(any(), any());
    }

    @Test
    void weeklyJob_skipsUserWithNoMeals() {
        when(mealLogRepository.findDistinctUserIdsByLoggedAtBetween(any(), any()))
                .thenReturn(List.of());

        scheduler.generateWeeklyInsights();

        verify(adviceService, never()).generateAdvice(any(), any());
    }

    @Test
    void weeklyJob_generatesInsightForEligibleUser() {
        UUID userId = UUID.randomUUID();
        LocalDate monday = LocalDate.now(ZoneOffset.UTC).with(DayOfWeek.MONDAY);

        when(mealLogRepository.findDistinctUserIdsByLoggedAtBetween(any(), any()))
                .thenReturn(List.of(userId));
        when(adviceRepository.findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(
                userId, AdviceType.WEEKLY, monday))
                .thenReturn(List.of());

        scheduler.generateWeeklyInsights();

        ArgumentCaptor<GenerateAdviceRequest> captor = ArgumentCaptor.forClass(GenerateAdviceRequest.class);
        verify(adviceService).generateAdvice(eq(userId), captor.capture());
        assertThat(captor.getValue().adviceType()).isEqualTo(AdviceType.WEEKLY);
        assertThat(captor.getValue().periodStart()).isEqualTo(monday);
        assertThat(captor.getValue().preview()).isFalse();
    }

    @Test
    void weeklyJob_periodStartIsMonday() {
        UUID userId = UUID.randomUUID();
        LocalDate monday = LocalDate.now(ZoneOffset.UTC).with(DayOfWeek.MONDAY);

        when(mealLogRepository.findDistinctUserIdsByLoggedAtBetween(any(), any()))
                .thenReturn(List.of(userId));
        when(adviceRepository.findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(List.of());

        scheduler.generateWeeklyInsights();

        ArgumentCaptor<GenerateAdviceRequest> captor = ArgumentCaptor.forClass(GenerateAdviceRequest.class);
        verify(adviceService).generateAdvice(any(), captor.capture());
        assertThat(captor.getValue().periodStart().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(captor.getValue().periodStart()).isEqualTo(monday);
    }
}
