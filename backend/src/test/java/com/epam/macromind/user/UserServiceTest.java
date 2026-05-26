package com.epam.macromind.user;

import com.epam.macromind.auth.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository repository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService service;

    private static final CreateUserRequest VALID_REQUEST = new CreateUserRequest(
            "Alice", "alice@example.com", 30,
            new BigDecimal("65.0"), new BigDecimal("170.0"), GoalType.MAINTAIN_WEIGHT);

    @Test
    void createUser_success_returnsUserResponse() {
        User saved = new User("Alice", "alice@example.com", "", 30,
                new BigDecimal("65.0"), new BigDecimal("170.0"), GoalType.MAINTAIN_WEIGHT);
        when(repository.save(any())).thenReturn(saved);

        UserResponse response = service.createUser(VALID_REQUEST);

        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.goalType()).isEqualTo(GoalType.MAINTAIN_WEIGHT);
    }

    @Test
    void createUser_duplicateEmail_throwsEmailAlreadyExistsException() {
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("unique constraint"));

        assertThatThrownBy(() -> service.createUser(VALID_REQUEST))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("alice@example.com");
    }

    @Test
    void getUserById_existingId_returnsUserResponse() {
        UUID id = UUID.randomUUID();
        User user = new User("Bob", "bob@example.com", "", 25,
                new BigDecimal("80.0"), new BigDecimal("180.0"), GoalType.GAIN_MUSCLE);
        when(repository.findById(id)).thenReturn(Optional.of(user));

        UserResponse response = service.getUserById(id);

        assertThat(response.email()).isEqualTo("bob@example.com");
    }

    @Test
    void getUserById_unknownId_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    private static final UpdateUserRequest UPDATE_REQUEST = new UpdateUserRequest(
            "Alice Updated", 31, new BigDecimal("63.0"), new BigDecimal("170.0"), GoalType.LOSE_WEIGHT);

    @Test
    void updateUser_success_returnsUpdatedResponse() {
        UUID id = UUID.randomUUID();
        User existing = new User("Alice", "alice@example.com", "", 30,
                new BigDecimal("65.0"), new BigDecimal("170.0"), GoalType.MAINTAIN_WEIGHT);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        UserResponse response = service.updateUser(id, UPDATE_REQUEST);

        assertThat(response.name()).isEqualTo("Alice Updated");
        assertThat(response.age()).isEqualTo(31);
        assertThat(response.goalType()).isEqualTo(GoalType.LOSE_WEIGHT);
    }

    @Test
    void updateUser_unknownId_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateUser(id, UPDATE_REQUEST))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void updatePassword_success_savesNewHash() {
        UUID id = UUID.randomUUID();
        User user = new User("Alice", "alice@example.com", "$2a$hash", 30,
                new BigDecimal("65.0"), new BigDecimal("170.0"), GoalType.MAINTAIN_WEIGHT);
        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldpass", "$2a$hash")).thenReturn(true);
        when(passwordEncoder.encode("newpass123")).thenReturn("$2a$newhash");
        when(repository.save(user)).thenReturn(user);

        service.updatePassword(id, "oldpass", "newpass123");

        assertThat(user.getPasswordHash()).isEqualTo("$2a$newhash");
        verify(repository).save(user);
    }

    @Test
    void updatePassword_wrongCurrentPassword_throwsInvalidCredentials() {
        UUID id = UUID.randomUUID();
        User user = new User("Alice", "alice@example.com", "$2a$hash", 30,
                new BigDecimal("65.0"), new BigDecimal("170.0"), GoalType.MAINTAIN_WEIGHT);
        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "$2a$hash")).thenReturn(false);

        assertThatThrownBy(() -> service.updatePassword(id, "wrongpass", "newpass123"))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void updatePassword_unknownUser_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePassword(id, "any", "newpass123"))
                .isInstanceOf(UserNotFoundException.class);
    }
}
