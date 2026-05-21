package com.epam.macromind.dashboard;

import java.time.LocalDate;

record DailyEntry(
        LocalDate date,
        MacroTotals totals
) {}
