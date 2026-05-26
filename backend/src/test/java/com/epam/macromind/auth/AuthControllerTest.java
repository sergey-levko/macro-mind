package com.epam.macromind.auth;

import com.epam.macromind.common.GlobalExceptionHandler;
import com.epam.macromind.user.GoalType;
import com.epam.macromind.user.UserResponse;
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
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean AuthService authService;
    @MockitoBean JwtService jwtService;

    private static final UserResponse USER_RESPONSE = new UserResponse(
            UUID.randomUUID(), "Alice", "alice@example.com", 30,
            new BigDecimal("65.0"), new BigDecimal("170.0"), GoalType.MAINTAIN_WEIGHT);

    @Test
    void login_returns200WithBothTokens() throws Exception {
        when(authService.login(any())).thenReturn(
                new AuthResponse("access-token", "refresh-token", USER_RESPONSE));

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new LoginRequest("alice@example.com", "password1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void register_returns201WithBothTokens() throws Exception {
        when(authService.register(any())).thenReturn(
                new AuthResponse("access-token", "refresh-token", USER_RESPONSE));

        String body = mapper.writeValueAsString(new RegisterRequest(
                "Alice", "alice@example.com", "password1",
                30, new BigDecimal("65.0"), new BigDecimal("170.0"), GoalType.MAINTAIN_WEIGHT));

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void refresh_validToken_returns200() throws Exception {
        when(authService.refresh("valid-refresh"))
                .thenReturn(new RefreshResponse("new-access", "new-refresh"));

        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("refreshToken", "valid-refresh"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    void refresh_invalidToken_returns401() throws Exception {
        when(authService.refresh("bad-token"))
                .thenThrow(new InvalidRefreshTokenException("Refresh token not found"));

        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("refreshToken", "bad-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token not found"));
    }

    @Test
    @WithMockUser
    void logout_returns204() throws Exception {
        doNothing().when(authService).logout(any());

        mvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("refreshToken", "some-token"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_unauthenticated_returns401() throws Exception {
        mvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("refreshToken", "some-token"))))
                .andExpect(status().isUnauthorized());
    }
}
