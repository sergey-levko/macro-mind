package com.epam.macromind.dashboard;

import java.time.LocalDate;

record DailyDashboardResponse(
        LocalDate date,
        MacroTotals totals,
        MacroTargets targets
) {}
