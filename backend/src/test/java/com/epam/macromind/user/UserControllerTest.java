package com.epam.macromind.user;

import com.epam.macromind.common.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean UserService service;

    private static final UUID USER_ID = UUID.randomUUID();

    private static final UserResponse SAMPLE_RESPONSE = new UserResponse(
            USER_ID, "Alice", "alice@example.com", 30,
            new BigDecimal("65.0"), new BigDecimal("170.0"), GoalType.MAINTAIN_WEIGHT);

    private static final CreateUserRequest VALID_REQUEST = new CreateUserRequest(
            "Alice", "alice@example.com", 30,
            new BigDecimal("65.0"), new BigDecimal("170.0"), GoalType.MAINTAIN_WEIGHT);

    @Test
    void register_validBody_returns201() throws Exception {
        when(service.createUser(any())).thenReturn(SAMPLE_RESPONSE);

        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(VALID_REQUEST)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(service.createUser(any())).thenThrow(new EmailAlreadyExistsException("alice@example.com"));

        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(VALID_REQUEST)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void register_missingFields_returns400() throws Exception {
        String body = """
                {"email": "alice@example.com"}
                """;

        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidGoalType_returns400() throws Exception {
        String body = """
                {"name":"Alice","email":"a@b.com","age":30,"weightKg":65,"heightCm":170,"goalType":"INVALID"}
                """;

        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_existingId_returns200() throws Exception {
        when(service.getUserById(USER_ID)).thenReturn(SAMPLE_RESPONSE);

        mvc.perform(get("/api/v1/users/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void getById_unknownId_returns404() throws Exception {
        UUID unknown = UUID.randomUUID();
        when(service.getUserById(unknown)).thenThrow(new UserNotFoundException(unknown));

        mvc.perform(get("/api/v1/users/{id}", unknown))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getById_invalidUuidFormat_returns400() throws Exception {
        mvc.perform(get("/api/v1/users/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
}
