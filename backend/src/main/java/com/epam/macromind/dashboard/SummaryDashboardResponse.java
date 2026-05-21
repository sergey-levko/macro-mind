package com.epam.macromind.dashboard;

import java.time.LocalDate;

record SummaryDashboardResponse(
        LocalDate date,
        MacroTotals totals,
        MacroTargets targets,
        MacroPercentages percentages
) {}
