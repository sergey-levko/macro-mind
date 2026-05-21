package com.epam.macromind.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
class UserController {

    private final UserService service;

    UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserResponse register(@Valid @RequestBody CreateUserRequest request) {
        return service.createUser(request);
    }

    @GetMapping("/{id}")
    UserResponse getById(@PathVariable UUID id) {
        return service.getUserById(id);
    }

    @PutMapping("/{id}")
    UserResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return service.updateUser(id, request);
    }
}
