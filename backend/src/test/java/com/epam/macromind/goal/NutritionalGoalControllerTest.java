package com.epam.macromind.goal;

import com.epam.macromind.auth.JwtService;
import com.epam.macromind.auth.SecurityConfig;
import com.epam.macromind.common.GlobalExceptionHandler;
import com.epam.macromind.user.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NutritionalGoalController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@WithMockUser(username = "00000000-0000-0000-0000-000000000001")
class NutritionalGoalControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean NutritionalGoalService service;
    @MockitoBean JwtService jwtService;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID GOAL_ID = UUID.randomUUID();

    private static final NutritionalGoalResponse SAMPLE_RESPONSE = new NutritionalGoalResponse(
            GOAL_ID, USER_ID, new BigDecimal("2000"), new BigDecimal("150"),
            new BigDecimal("200"), new BigDecimal("70"));

    private static final String VALID_BODY =
            "{\"caloriesTarget\":2000,\"proteinG\":150,\"carbsG\":200,\"fatG\":70}";

    @Test
    void setGoal_validRequest_returns200() throws Exception {
        when(service.setGoal(eq(USER_ID), any())).thenReturn(SAMPLE_RESPONSE);

        mvc.perform(put("/api/v1/nutritional-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.caloriesTarget").value(2000));
    }

    @Test
    void setGoal_missingField_returns400() throws Exception {
        mvc.perform(put("/api/v1/nutritional-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caloriesTarget\":2000}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setGoal_negativeValue_returns400() throws Exception {
        mvc.perform(put("/api/v1/nutritional-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caloriesTarget\":-1,\"proteinG\":150,\"carbsG\":200,\"fatG\":70}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setGoal_unknownUser_returns404() throws Exception {
        when(service.setGoal(eq(USER_ID), any()))
                .thenThrow(new UserNotFoundException(USER_ID));

        mvc.perform(put("/api/v1/nutritional-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGoal_exists_returns200() throws Exception {
        when(service.getGoal(USER_ID)).thenReturn(SAMPLE_RESPONSE);

        mvc.perform(get("/api/v1/nutritional-goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(GOAL_ID.toString()));
    }

    @Test
    void getGoal_notFound_returns404() throws Exception {
        when(service.getGoal(USER_ID)).thenThrow(new GoalNotFoundException(USER_ID));

        mvc.perform(get("/api/v1/nutritional-goals"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteGoal_success_returns204() throws Exception {
        mvc.perform(delete("/api/v1/nutritional-goals"))
                .andExpect(status().isNoContent());

        verify(service).deleteGoal(USER_ID);
    }

    @Test
    void deleteGoal_notFound_returns404() throws Exception {
        doThrow(new GoalNotFoundException(USER_ID)).when(service).deleteGoal(USER_ID);

        mvc.perform(delete("/api/v1/nutritional-goals"))
                .andExpect(status().isNotFound());
    }
}
