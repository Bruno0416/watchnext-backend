package com.watchnext.auth_service.service;

import com.watchnext.auth_service.dto.AuthResponse;
import com.watchnext.auth_service.dto.LoginRequest;
import com.watchnext.auth_service.dto.RegisterRequest;
import com.watchnext.auth_service.entity.Role;
import com.watchnext.auth_service.entity.Users;
import com.watchnext.auth_service.exceptions.EmailAlreadyInUse;
import com.watchnext.auth_service.exceptions.InvalidCredentials;
import com.watchnext.auth_service.exceptions.InvalidRefreshToken;
import com.watchnext.auth_service.repository.UserRepository;
import com.watchnext.auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtService;
    private final AuthenticationManager authManager;

    @Override
    public void register(RegisterRequest request) {
        // 1. validar si el usuario ya tiene cuenta
        if (repo.existsByEmail(request.email())) {
            throw new EmailAlreadyInUse(request.email());
        }

        // 2. registrar usuario
        repo.save(
            Users.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build()
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. autenticar usuario
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.email(),
                    request.password()
                )
            );
        } catch (Exception e) {
            throw new InvalidCredentials("Credenciales invalidas");
        }

        // 2. buscar usuario en el repo
        Users user = repo
            .findByEmail(request.email())
            .orElseThrow(() ->
                new InvalidCredentials("Credenciales invalidas")
            );

        // 3. generar tokens y retornar
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        // 1. extraer el email del token
        String email = jwtService.getUsernameFromToken(refreshToken);

        if (email != null) {
            Users user = repo
                .findByEmail(email)
                .orElseThrow(() ->
                    new InvalidCredentials("Credentials are invalid")
                );

            if (jwtService.isTokenValid(refreshToken, user)) {
                String newAccessToken = jwtService.generateToken(user);
                String newRefreshToken = jwtService.generateRefreshToken(user);

                return new AuthResponse(newAccessToken, newRefreshToken);
            }
        }
        throw new InvalidRefreshToken("Invalid refresh token");
    }
}
