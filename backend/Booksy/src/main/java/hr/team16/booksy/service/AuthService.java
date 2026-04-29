package hr.team16.booksy.service;

import hr.team16.booksy.dto.AuthResponse;
import hr.team16.booksy.dto.LoginRequest;
import hr.team16.booksy.dto.RegisterRequest;
import hr.team16.booksy.model.User;
import hr.team16.booksy.repository.UserRepository;
import hr.team16.booksy.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
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

        String token = jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());
        return new AuthResponse(token, user.getRole(), user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());
        return new AuthResponse(token, user.getRole(), user.getEmail());
    }
}