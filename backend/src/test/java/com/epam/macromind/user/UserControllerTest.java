package com.epam.macromind.user;

import com.epam.macromind.auth.JwtService;
import com.epam.macromind.common.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser(username = "00000000-0000-0000-0000-000000000001")
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean UserService service;
    @MockitoBean JwtService jwtService;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static final UserResponse SAMPLE_RESPONSE = new UserResponse(
            USER_ID, "Alice", "alice@example.com", 30,
            new BigDecimal("65.0"), new BigDecimal("170.0"), GoalType.MAINTAIN_WEIGHT);

    @Test
    void getMe_returns200() throws Exception {
        when(service.getUserById(USER_ID)).thenReturn(SAMPLE_RESPONSE);

        mvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void updateMe_validBody_returns200() throws Exception {
        when(service.updateUser(any(), any())).thenReturn(SAMPLE_RESPONSE);

        UpdateUserRequest update = new UpdateUserRequest(
                "Alice", 30, new BigDecimal("65.0"), new BigDecimal("170.0"), GoalType.MAINTAIN_WEIGHT);

        mvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void updateMe_missingName_returns400() throws Exception {
        String body = "{\"age\":30,\"weightKg\":65,\"heightCm\":170,\"goalType\":\"MAINTAIN_WEIGHT\"}";

        mvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMe_notFound_returns404() throws Exception {
        when(service.getUserById(USER_ID)).thenThrow(new UserNotFoundException(USER_ID));

        mvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}
