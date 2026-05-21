package com.epam.macromind.dashboard;

import java.time.LocalDate;
import java.util.List;

record WeeklyDashboardResponse(
        LocalDate weekStart,
        List<DailyEntry> days,
        MacroTotals weeklyTotals,
        MacroTargets weeklyTargets
) {}
