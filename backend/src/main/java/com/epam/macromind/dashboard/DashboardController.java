package com.epam.macromind.dashboard;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
class DashboardController {

    private final DashboardService dashboardService;

    DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/daily")
    DailyDashboardResponse getDaily(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dashboardService.getDailySummary(userId, date);
    }

    @GetMapping("/weekly")
    WeeklyDashboardResponse getWeekly(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return dashboardService.getWeeklySummary(userId, weekStart);
    }

    @GetMapping("/summary")
    SummaryDashboardResponse getSummary(
            @RequestHeader("X-User-Id") UUID userId) {
        return dashboardService.getSummaryCard(userId);
    }
}
