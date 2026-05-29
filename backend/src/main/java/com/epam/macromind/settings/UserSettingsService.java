package com.epam.macromind.settings;

import com.epam.macromind.user.User;
import com.epam.macromind.user.UserNotFoundException;
import com.epam.macromind.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
class UserSettingsService {

    private final UserRepository userRepository;

    UserSettingsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    UserSettingsResponse getSettings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return new UserSettingsResponse(user.isUsdaEnabled());
    }

    UserSettingsResponse updateSettings(UUID userId, UpdateUserSettingsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setUsdaEnabled(request.usdaEnabled());
        userRepository.save(user);
        return new UserSettingsResponse(user.isUsdaEnabled());
    }
}
