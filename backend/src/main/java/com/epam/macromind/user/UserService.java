package com.epam.macromind.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
class UserService {

    private final UserRepository repository;

    UserService(UserRepository repository) {
        this.repository = repository;
    }

    UserResponse createUser(CreateUserRequest request) {
        try {
            User user = new User(request.name(), request.email(), request.age(),
                    request.weightKg(), request.heightCm(), request.goalType());
            return UserResponse.from(repository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(request.email());
        }
    }

    UserResponse getUserById(UUID id) {
        return repository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
