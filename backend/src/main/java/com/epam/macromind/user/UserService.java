package com.epam.macromind.user;

import com.epam.macromind.auth.InvalidCredentialsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    UserResponse createUser(CreateUserRequest request) {
        try {
            User user = new User(request.name(), request.email(), "",
                    request.age(), request.weightKg(), request.heightCm(), request.goalType());
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

    void updatePassword(UUID userId, String currentPassword, String newPassword) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        user.updatePasswordHash(passwordEncoder.encode(newPassword));
        repository.save(user);
    }

    UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.update(request.name(), request.age(), request.weightKg(),
                request.heightCm(), request.goalType());
        return UserResponse.from(repository.save(user));
    }
}
