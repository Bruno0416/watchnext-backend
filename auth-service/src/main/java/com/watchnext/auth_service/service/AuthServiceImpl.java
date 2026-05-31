package com.watchnext.auth_service.service;

import com.watchnext.auth_service.dto.AuthResponse;
import com.watchnext.auth_service.dto.LoginRequest;
import com.watchnext.auth_service.dto.RegisterRequest;
import com.watchnext.auth_service.entity.Role;
import com.watchnext.auth_service.entity.Users;
import com.watchnext.auth_service.exceptions.EmailAlreadyInUseException;
import com.watchnext.auth_service.exceptions.InvalidCredentialsException;
import com.watchnext.auth_service.exceptions.InvalidRefreshTokenException;
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
    public AuthResponse register(RegisterRequest request) {
        // 1. validar si el usuario ya tiene cuenta
        if (repo.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyInUseException(request.getEmail());
        }

        // 2. construir entidad usuasrio
        Users user = repo.save(
            Users.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build()
        );

        // 3. generar tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
            .token(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. autenticar usuario
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
        } catch (Exception e) {
            throw new InvalidCredentialsException("Credenciales invalidas");
        }

        // 2. buscar usuario en el repo
        Users user = repo
            .findByEmail(request.getEmail())
            .orElseThrow(() ->
                new InvalidCredentialsException("Credenciales invalidas")
            );

        // 3. generar tokens y retornar
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
            .token(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        // 1. extraer el email del token
        String email = jwtService.getUsernameFromToken(refreshToken);

        if (email != null) {
            Users user = repo
                .findByEmail(email)
                .orElseThrow(() ->
                    new InvalidCredentialsException("Credentials are invalid")
                );

            if (jwtService.isTokenValid(refreshToken, user)) {
                String newAccessToken = jwtService.generateToken(user);
                String newRefreshToken = jwtService.generateRefreshToken(user);

                return AuthResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();
            }
        }
        throw new InvalidRefreshTokenException("Invalid refresh token");
    }
}
