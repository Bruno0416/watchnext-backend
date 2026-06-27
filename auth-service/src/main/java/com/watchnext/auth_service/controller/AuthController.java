package com.watchnext.auth_service.controller;

import com.watchnext.auth_service.dto.AuthResponse;
import com.watchnext.auth_service.dto.LoginRequest;
import com.watchnext.auth_service.dto.RefreshTokenRequest;
import com.watchnext.auth_service.dto.RegisterRequest;
import com.watchnext.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(
            authService.refreshToken(request.refreshToken())
        );
    }
}
