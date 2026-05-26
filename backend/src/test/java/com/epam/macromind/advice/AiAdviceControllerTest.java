package com.epam.macromind.advice;

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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AiAdviceController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@WithMockUser(username = "00000000-0000-0000-0000-000000000001")
class AiAdviceControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean AiAdviceService adviceService;
    @MockitoBean JwtService jwtService;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ADVICE_ID = UUID.randomUUID();
    private static final LocalDate TODAY = LocalDate.of(2026, 5, 20);

    private static final AiAdviceResponse SAMPLE_RESPONSE = new AiAdviceResponse(
            ADVICE_ID, USER_ID, AdviceType.DAILY, TODAY, "Eat more protein.", AdviceStatus.COMPLETED, Instant.now());

    private static final String VALID_BODY =
            "{\"adviceType\":\"DAILY\",\"periodStart\":\"2026-05-20\"}";

    @Test
    void generateAdvice_newRecord_returns202() throws Exception {
        when(adviceService.generateAdvice(eq(USER_ID), any()))
                .thenReturn(new GenerateAdviceResult(SAMPLE_RESPONSE, true));

        mvc.perform(post("/api/v1/advice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.adviceType").value("DAILY"));
    }

    @Test
    void generateAdvice_duplicateRecord_returns200() throws Exception {
        when(adviceService.generateAdvice(eq(USER_ID), any()))
                .thenReturn(new GenerateAdviceResult(SAMPLE_RESPONSE, false));

        mvc.perform(post("/api/v1/advice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ADVICE_ID.toString()));
    }

    @Test
    void generateAdvice_missingFields_returns400() throws Exception {
        mvc.perform(post("/api/v1/advice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateAdvice_unknownUser_returns404() throws Exception {
        when(adviceService.generateAdvice(eq(USER_ID), any()))
                .thenThrow(new UserNotFoundException(USER_ID));

        mvc.perform(post("/api/v1/advice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void generateAdvice_noGoal_returns400() throws Exception {
        when(adviceService.generateAdvice(eq(USER_ID), any()))
                .thenThrow(new NoGoalForAdviceException(USER_ID));

        mvc.perform(post("/api/v1/advice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAdvice_success_returns204() throws Exception {
        mvc.perform(delete("/api/v1/advice/{id}", ADVICE_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAdvice_notFound_returns404() throws Exception {
        doThrow(new AdviceNotFoundException(ADVICE_ID))
                .when(adviceService).deleteAdvice(USER_ID, ADVICE_ID);

        mvc.perform(delete("/api/v1/advice/{id}", ADVICE_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAdvice_exists_returns200() throws Exception {
        when(adviceService.getAdvice(ADVICE_ID)).thenReturn(SAMPLE_RESPONSE);

        mvc.perform(get("/api/v1/advice/{id}", ADVICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ADVICE_ID.toString()));
    }

    @Test
    void getAdvice_notFound_returns404() throws Exception {
        when(adviceService.getAdvice(ADVICE_ID)).thenThrow(new AdviceNotFoundException(ADVICE_ID));

        mvc.perform(get("/api/v1/advice/{id}", ADVICE_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void listAdvice_noFilters_returns200() throws Exception {
        when(adviceService.listAdvice(USER_ID, null, null)).thenReturn(List.of(SAMPLE_RESPONSE));

        mvc.perform(get("/api/v1/advice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].adviceType").value("DAILY"));
    }
}
