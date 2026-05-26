package com.epam.macromind.dashboard;

import com.epam.macromind.common.SecurityUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard")
class DashboardController {

    private final DashboardService dashboardService;

    DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/daily")
    DailyDashboardResponse getDaily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dashboardService.getDailySummary(SecurityUtils.currentUserId(), date);
    }

    @GetMapping("/weekly")
    WeeklyDashboardResponse getWeekly(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return dashboardService.getWeeklySummary(SecurityUtils.currentUserId(), weekStart);
    }

    @GetMapping("/summary")
    SummaryDashboardResponse getSummary() {
        return dashboardService.getSummaryCard(SecurityUtils.currentUserId());
    }
}
