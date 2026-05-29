package com.epam.macromind.settings;

import com.epam.macromind.user.GoalType;
import com.epam.macromind.user.User;
import com.epam.macromind.user.UserNotFoundException;
import com.epam.macromind.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSettingsServiceTest {

    @Mock UserRepository userRepository;

    UserSettingsService service;

    @BeforeEach
    void setUp() {
        service = new UserSettingsService(userRepository);
    }

    @Test
    void getSettings_returnsCurrentUsdaEnabled() {
        UUID userId = UUID.randomUUID();
        User user = new User("Test", "test@example.com", "hash", 30,
                new BigDecimal("70"), new BigDecimal("175"), GoalType.MAINTAIN_WEIGHT);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserSettingsResponse result = service.getSettings(userId);

        assertThat(result.usdaEnabled()).isTrue();
    }

    @Test
    void getSettings_userNotFound_throwsUserNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSettings(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateSettings_persistsNewValue() {
        UUID userId = UUID.randomUUID();
        User user = new User("Test", "test@example.com", "hash", 30,
                new BigDecimal("70"), new BigDecimal("175"), GoalType.MAINTAIN_WEIGHT);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserSettingsResponse result = service.updateSettings(userId, new UpdateUserSettingsRequest(false));

        assertThat(result.usdaEnabled()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void updateSettings_toggleOffThenOn_reflectsLatestValue() {
        UUID userId = UUID.randomUUID();
        User user = new User("Test", "test@example.com", "hash", 30,
                new BigDecimal("70"), new BigDecimal("175"), GoalType.MAINTAIN_WEIGHT);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        service.updateSettings(userId, new UpdateUserSettingsRequest(false));
        assertThat(user.isUsdaEnabled()).isFalse();

        service.updateSettings(userId, new UpdateUserSettingsRequest(true));
        assertThat(user.isUsdaEnabled()).isTrue();
    }

    @Test
    void updateSettings_userNotFound_throwsUserNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSettings(userId, new UpdateUserSettingsRequest(false)))
                .isInstanceOf(UserNotFoundException.class);
    }
}
