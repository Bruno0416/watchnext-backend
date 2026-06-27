package com.watchnext.auth_service.service;

import com.watchnext.auth_service.dto.AuthResponse;
import com.watchnext.auth_service.dto.LoginRequest;
import com.watchnext.auth_service.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);
}
