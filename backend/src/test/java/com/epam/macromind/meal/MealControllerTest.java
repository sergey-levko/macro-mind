package com.epam.macromind.meal;

import com.epam.macromind.common.GlobalExceptionHandler;
import com.epam.macromind.food.FoodNotFoundException;
import com.epam.macromind.user.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MealController.class)
@Import(GlobalExceptionHandler.class)
class MealControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean MealService service;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID LOG_ID = UUID.randomUUID();
    private static final UUID ITEM_ID = UUID.randomUUID();
    private static final UUID FOOD_ID = UUID.randomUUID();

    private static final MealLogResponse SAMPLE_LOG = new MealLogResponse(
            LOG_ID, USER_ID, MealType.BREAKFAST, Instant.now(), List.of(),
            new MacroTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

    private static final MealItemResponse SAMPLE_ITEM = new MealItemResponse(
            ITEM_ID, FOOD_ID, "Chicken", new BigDecimal("150"),
            new BigDecimal("247.50"), new BigDecimal("46.50"), BigDecimal.ZERO, new BigDecimal("5.40"));

    @Test
    void create_validRequest_returns201() throws Exception {
        when(service.createMealLog(eq(USER_ID), any())).thenReturn(SAMPLE_LOG);

        mvc.perform(post("/api/v1/meal-logs")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mealType\":\"BREAKFAST\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mealType").value("BREAKFAST"));
    }

    @Test
    void create_missingMealType_returns400() throws Exception {
        mvc.perform(post("/api/v1/meal-logs")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_unknownUser_returns404() throws Exception {
        when(service.createMealLog(eq(USER_ID), any()))
                .thenThrow(new UserNotFoundException(USER_ID));

        mvc.perform(post("/api/v1/meal-logs")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mealType\":\"LUNCH\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_exists_returns200() throws Exception {
        when(service.getMealLogById(LOG_ID)).thenReturn(SAMPLE_LOG);

        mvc.perform(get("/api/v1/meal-logs/" + LOG_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(LOG_ID.toString()));
    }

    @Test
    void getById_unknownLog_returns404() throws Exception {
        when(service.getMealLogById(LOG_ID))
                .thenThrow(new MealLogNotFoundException(LOG_ID));

        mvc.perform(get("/api/v1/meal-logs/" + LOG_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void listByDate_validDate_returns200() throws Exception {
        when(service.getMealLogsByDate(eq(USER_ID), any())).thenReturn(List.of());

        mvc.perform(get("/api/v1/meal-logs?date=2024-01-15")
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk());
    }

    @Test
    void listByDate_missingDate_returns400() throws Exception {
        mvc.perform(get("/api/v1/meal-logs")
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_success_returns204() throws Exception {
        mvc.perform(delete("/api/v1/meal-logs/" + LOG_ID)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isNoContent());

        verify(service).deleteMealLog(USER_ID, LOG_ID);
    }

    @Test
    void delete_forbidden_returns403() throws Exception {
        doThrow(new MealLogAccessDeniedException(LOG_ID))
                .when(service).deleteMealLog(USER_ID, LOG_ID);

        mvc.perform(delete("/api/v1/meal-logs/" + LOG_ID)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    void addItem_validRequest_returns201() throws Exception {
        when(service.addItem(eq(USER_ID), eq(LOG_ID), any())).thenReturn(SAMPLE_ITEM);

        mvc.perform(post("/api/v1/meal-logs/" + LOG_ID + "/items")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"foodId\":\"" + FOOD_ID + "\",\"quantityG\":150}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.foodName").value("Chicken"));
    }

    @Test
    void addItem_invalidQuantityG_returns400() throws Exception {
        mvc.perform(post("/api/v1/meal-logs/" + LOG_ID + "/items")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"foodId\":\"" + FOOD_ID + "\",\"quantityG\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_unknownFood_returns404() throws Exception {
        when(service.addItem(eq(USER_ID), eq(LOG_ID), any()))
                .thenThrow(new FoodNotFoundException(FOOD_ID));

        mvc.perform(post("/api/v1/meal-logs/" + LOG_ID + "/items")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"foodId\":\"" + FOOD_ID + "\",\"quantityG\":100}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeItem_success_returns204() throws Exception {
        mvc.perform(delete("/api/v1/meal-logs/" + LOG_ID + "/items/" + ITEM_ID)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isNoContent());

        verify(service).removeItem(USER_ID, LOG_ID, ITEM_ID);
    }
}
