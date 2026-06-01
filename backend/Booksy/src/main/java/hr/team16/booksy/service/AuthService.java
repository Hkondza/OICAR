package hr.team16.booksy.service;

import hr.team16.booksy.dto.AuthResponse;
import hr.team16.booksy.dto.LoginRequest;
import hr.team16.booksy.dto.RegisterRequest;
import hr.team16.booksy.model.RefreshToken;
import hr.team16.booksy.model.User;
import hr.team16.booksy.repository.RefreshTokenRepository;
import hr.team16.booksy.repository.UserRepository;
import hr.team16.booksy.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int REFRESH_TOKEN_EXPIRY_DAYS = 7;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "GUEST");
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());
        String refreshToken = createRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, user.getRole(), user.getEmail(), user.getId());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        refreshTokenRepository.revokeAllByUser(user);

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());
        String refreshToken = createRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, user.getRole(), user.getEmail(), user.getId());
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token nije pronađen"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token je poništen");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token je istekao – potrebna je nova prijava");
        }

        User user = refreshToken.getUser();

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());
        String newRefreshToken = createRefreshToken(user);
        return new AuthResponse(newAccessToken, newRefreshToken, user.getRole(), user.getEmail(), user.getId());
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    private String createRefreshToken(User user) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUser(user);
        rt.setExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS));
        rt.setRevoked(false);
        refreshTokenRepository.save(rt);
        return rt.getToken();
    }
}