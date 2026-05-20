package com.epam.macromind.food;

import com.epam.macromind.common.GlobalExceptionHandler;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FoodController.class)
@Import(GlobalExceptionHandler.class)
class FoodControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean FoodService service;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID FOOD_ID = UUID.randomUUID();

    private static final FoodResponse SAMPLE_RESPONSE = new FoodResponse(
            FOOD_ID, "Chicken Breast", "CUSTOM",
            new BigDecimal("165"), new BigDecimal("31"),
            new BigDecimal("0"), new BigDecimal("3.6"));

    private static final CreateFoodRequest VALID_REQUEST = new CreateFoodRequest(
            "Chicken Breast", new BigDecimal("165"), new BigDecimal("31"),
            new BigDecimal("0"), new BigDecimal("3.6"));

    @Test
    void create_validRequest_returns201() throws Exception {
        when(service.createFood(eq(USER_ID), any())).thenReturn(SAMPLE_RESPONSE);

        mvc.perform(post("/api/v1/foods")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(VALID_REQUEST)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(FOOD_ID.toString()))
                .andExpect(jsonPath("$.source").value("CUSTOM"));
    }

    @Test
    void create_missingFields_returns400() throws Exception {
        mvc.perform(post("/api/v1/foods")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_unknownUser_returns404() throws Exception {
        when(service.createFood(eq(USER_ID), any())).thenThrow(new UserNotFoundException(USER_ID));

        mvc.perform(post("/api/v1/foods")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(VALID_REQUEST)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getById_exists_returns200() throws Exception {
        when(service.getFoodById(FOOD_ID)).thenReturn(SAMPLE_RESPONSE);

        mvc.perform(get("/api/v1/foods/{id}", FOOD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chicken Breast"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(service.getFoodById(FOOD_ID)).thenThrow(new FoodNotFoundException(FOOD_ID));

        mvc.perform(get("/api/v1/foods/{id}", FOOD_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getById_invalidUuid_returns400() throws Exception {
        mvc.perform(get("/api/v1/foods/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_returns200WithList() throws Exception {
        when(service.searchFoods(eq(USER_ID), eq("chicken"))).thenReturn(List.of(SAMPLE_RESPONSE));

        mvc.perform(get("/api/v1/foods")
                        .header("X-User-Id", USER_ID)
                        .param("search", "chicken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Chicken Breast"));
    }

    @Test
    void delete_success_returns204() throws Exception {
        doNothing().when(service).deleteFood(USER_ID, FOOD_ID);

        mvc.perform(delete("/api/v1/foods/{id}", FOOD_ID)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_forbidden_returns403() throws Exception {
        doThrow(new FoodAccessDeniedException(FOOD_ID)).when(service).deleteFood(USER_ID, FOOD_ID);

        mvc.perform(delete("/api/v1/foods/{id}", FOOD_ID)
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void importFood_success_returns201() throws Exception {
        when(service.importFood(eq(USER_ID), any()))
                .thenReturn(new FoodResponse(FOOD_ID, "Brown Rice", "USDA",
                        new BigDecimal("370"), new BigDecimal("7.9"),
                        new BigDecimal("77"), new BigDecimal("2.9")));

        mvc.perform(post("/api/v1/foods/import")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fdcId\": 12345}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("USDA"));
    }

    @Test
    void importFood_unknownFdcId_returns404() throws Exception {
        when(service.importFood(eq(USER_ID), any())).thenThrow(new UsdaFoodNotFoundException(99999));

        mvc.perform(post("/api/v1/foods/import")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fdcId\": 99999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void importFood_usdaUnavailable_returns503() throws Exception {
        when(service.importFood(eq(USER_ID), any()))
                .thenThrow(new UsdaServiceUnavailableException("unreachable", null));

        mvc.perform(post("/api/v1/foods/import")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fdcId\": 1}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").exists());
    }
}
