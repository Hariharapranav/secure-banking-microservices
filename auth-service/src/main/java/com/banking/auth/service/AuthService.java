package com.banking.auth.service;

import com.banking.auth.dto.AuthResponse;
import com.banking.auth.dto.LoginRequest;
import com.banking.auth.dto.RegisterRequest;
import com.banking.auth.entity.UserCredential;
import com.banking.auth.exception.AuthException;
import com.banking.auth.repository.UserCredentialRepository;
import com.banking.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserCredentialRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user: {}", request.getUsername());

        if (repository.existsByUsername(request.getUsername())) {
            throw new AuthException("Username already exists: " + request.getUsername());
        }

        if (repository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already registered: " + request.getEmail());
        }

        UserCredential user = UserCredential.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserCredential.Role.USER)
                .enabled(true)
                .build();

        repository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole().name())
                .expiresIn(jwtService.getExpirationTime())
                .message("User registered successfully")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        if (authentication.isAuthenticated()) {
            UserCredential user = repository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new AuthException("User not found"));

            String token = jwtService.generateToken(user.getUsername(), user.getRole().name());

            log.info("User logged in successfully: {}", user.getUsername());

            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .username(user.getUsername())
                    .role(user.getRole().name())
                    .expiresIn(jwtService.getExpirationTime())
                    .message("Login successful")
                    .build();
        }

        throw new AuthException("Invalid credentials");
    }

    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }
}
