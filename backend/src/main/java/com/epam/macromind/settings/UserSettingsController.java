package com.epam.macromind.settings;

import com.epam.macromind.common.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
class UserSettingsController {

    private final UserSettingsService service;

    UserSettingsController(UserSettingsService service) {
        this.service = service;
    }

    @GetMapping
    ResponseEntity<UserSettingsResponse> getSettings() {
        return ResponseEntity.ok(service.getSettings(SecurityUtils.currentUserId()));
    }

    @PutMapping
    ResponseEntity<UserSettingsResponse> updateSettings(@Valid @RequestBody UpdateUserSettingsRequest request) {
        return ResponseEntity.ok(service.updateSettings(SecurityUtils.currentUserId(), request));
    }
}
