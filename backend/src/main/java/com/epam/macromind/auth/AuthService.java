package com.epam.macromind.auth;

import com.epam.macromind.user.EmailAlreadyExistsException;
import com.epam.macromind.user.User;
import com.epam.macromind.user.UserRepository;
import com.epam.macromind.user.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        String hash = passwordEncoder.encode(request.password());
        User user = new User(request.name(), request.email(), hash,
                request.age(), request.weightKg(), request.heightCm(), request.goalType());
        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getId());
        return new AuthResponse(token, UserResponse.from(saved));
    }

    AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        String token = jwtService.generateToken(user.getId());
        return new AuthResponse(token, UserResponse.from(user));
    }
}
