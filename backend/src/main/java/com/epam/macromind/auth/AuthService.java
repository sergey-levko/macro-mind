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
    private final RefreshTokenService refreshTokenService;

    AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        String hash = passwordEncoder.encode(request.password());
        User user = new User(request.name(), request.email(), hash,
                request.age(), request.weightKg(), request.heightCm(), request.goalType());
        User saved = userRepository.save(user);
        String accessToken = jwtService.generateToken(saved.getId());
        String refreshToken = refreshTokenService.createRefreshToken(saved.getId());
        return new AuthResponse(accessToken, refreshToken, UserResponse.from(saved));
    }

    AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        String accessToken = jwtService.generateToken(user.getId());
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return new AuthResponse(accessToken, refreshToken, UserResponse.from(user));
    }

    RefreshResponse refresh(String rawToken) {
        RotationResult result = refreshTokenService.validateAndRotate(rawToken);
        String newAccessToken = jwtService.generateToken(result.userId());
        return new RefreshResponse(newAccessToken, result.rawRefreshToken());
    }

    void logout(String rawToken) {
        refreshTokenService.revokeByRawToken(rawToken);
    }
}
