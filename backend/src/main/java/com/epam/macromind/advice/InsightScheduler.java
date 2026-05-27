package com.epam.macromind.advice;

import com.epam.macromind.meal.MealLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Component
class InsightScheduler {

    private static final Logger log = LoggerFactory.getLogger(InsightScheduler.class);

    private final MealLogRepository mealLogRepository;
    private final AiAdviceRepository adviceRepository;
    private final AiAdviceService adviceService;

    InsightScheduler(MealLogRepository mealLogRepository,
                     AiAdviceRepository adviceRepository,
                     AiAdviceService adviceService) {
        this.mealLogRepository = mealLogRepository;
        this.adviceRepository = adviceRepository;
        this.adviceService = adviceService;
    }

    @Scheduled(cron = "${insights.schedule.daily-cron:0 0 2 * * *}")
    void generateDailyInsights() {
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        var start = yesterday.atStartOfDay(ZoneOffset.UTC).toInstant();
        var end = yesterday.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<UUID> userIds = mealLogRepository.findDistinctUserIdsByLoggedAtBetween(start, end);
        log.info("Daily insight job: {} candidate users for {}", userIds.size(), yesterday);

        for (UUID userId : userIds) {
            var existing = adviceRepository.findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(
                    userId, AdviceType.DAILY, yesterday);
            if (!existing.isEmpty()) {
                continue;
            }
            try {
                adviceService.generateAdvice(userId, new GenerateAdviceRequest(AdviceType.DAILY, yesterday, false, null));
            } catch (Exception e) {
                log.error("Daily insight failed for user {}: {}", userId, e.getMessage());
            }
        }
    }

    @Scheduled(cron = "${insights.schedule.weekly-cron:0 30 23 * * SUN}")
    void generateWeeklyInsights() {
        LocalDate monday = LocalDate.now(ZoneOffset.UTC).with(DayOfWeek.MONDAY);
        var start = monday.atStartOfDay(ZoneOffset.UTC).toInstant();
        var end = monday.plusDays(7).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<UUID> userIds = mealLogRepository.findDistinctUserIdsByLoggedAtBetween(start, end);
        log.info("Weekly insight job: {} candidate users for week starting {}", userIds.size(), monday);

        for (UUID userId : userIds) {
            var existing = adviceRepository.findByUserIdAndAdviceTypeAndPeriodStartOrderByCreatedAtDesc(
                    userId, AdviceType.WEEKLY, monday);
            if (!existing.isEmpty()) {
                continue;
            }
            try {
                adviceService.generateAdvice(userId, new GenerateAdviceRequest(AdviceType.WEEKLY, monday, false, null));
            } catch (Exception e) {
                log.error("Weekly insight failed for user {}: {}", userId, e.getMessage());
            }
        }
    }
}
