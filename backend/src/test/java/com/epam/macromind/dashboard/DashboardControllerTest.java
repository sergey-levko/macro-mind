package com.epam.macromind.dashboard;

import com.epam.macromind.common.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import(GlobalExceptionHandler.class)
class DashboardControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean DashboardService dashboardService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final LocalDate DATE = LocalDate.of(2026, 5, 21);

    private static final MacroTotals ZERO_TOTALS = MacroTotals.zero();
    private static final MacroTargets TARGETS = new MacroTargets(
            new BigDecimal("2000"), new BigDecimal("150"),
            new BigDecimal("200"), new BigDecimal("70"));

    @Test
    void getDaily_returns200() throws Exception {
        when(dashboardService.getDailySummary(eq(USER_ID), eq(DATE)))
                .thenReturn(new DailyDashboardResponse(DATE, ZERO_TOTALS, TARGETS));

        mvc.perform(get("/api/v1/dashboard/daily")
                        .header("X-User-Id", USER_ID)
                        .param("date", "2026-05-21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-05-21"))
                .andExpect(jsonPath("$.targets.caloriesTarget").value(2000));
    }

    @Test
    void getDaily_missingDateParam_returns400() throws Exception {
        mvc.perform(get("/api/v1/dashboard/daily")
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWeekly_returns200() throws Exception {
        WeeklyDashboardResponse response = new WeeklyDashboardResponse(
                DATE, List.of(), ZERO_TOTALS, null);
        when(dashboardService.getWeeklySummary(eq(USER_ID), eq(DATE))).thenReturn(response);

        mvc.perform(get("/api/v1/dashboard/weekly")
                        .header("X-User-Id", USER_ID)
                        .param("weekStart", "2026-05-21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekStart").value("2026-05-21"));
    }

    @Test
    void getWeekly_missingWeekStartParam_returns400() throws Exception {
        mvc.perform(get("/api/v1/dashboard/weekly")
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSummary_returns200() throws Exception {
        MacroPercentages pct = new MacroPercentages(10, 7, 5, 3);
        SummaryDashboardResponse response = new SummaryDashboardResponse(DATE, ZERO_TOTALS, TARGETS, pct);
        when(dashboardService.getSummaryCard(any())).thenReturn(response);

        mvc.perform(get("/api/v1/dashboard/summary")
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentages.caloriesPct").value(10));
    }
}
