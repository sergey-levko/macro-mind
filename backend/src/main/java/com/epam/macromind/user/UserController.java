package com.epam.macromind.user;

import com.epam.macromind.common.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
class UserController {

    private final UserService service;

    UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    UserResponse getMe() {
        return service.getUserById(SecurityUtils.currentUserId());
    }

    @PutMapping("/me")
    UserResponse updateMe(@Valid @RequestBody UpdateUserRequest request) {
        return service.updateUser(SecurityUtils.currentUserId(), request);
    }
}
