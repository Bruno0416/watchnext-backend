package com.watchnext.auth_service.controller;

import com.watchnext.auth_service.dto.AuthResponse;
import com.watchnext.auth_service.dto.ConfirmEmailRequest;
import com.watchnext.auth_service.dto.LoginRequest;
import com.watchnext.auth_service.dto.RefreshTokenRequest;
import com.watchnext.auth_service.dto.RegisterRequest;
import com.watchnext.auth_service.dto.ResendCodeRequest;
import com.watchnext.auth_service.dto.reset.NewPasswordRequest;
import com.watchnext.auth_service.dto.reset.ResetPasswordRequest;
import com.watchnext.auth_service.dto.reset.VerifyResetCodeRequest;
import com.watchnext.auth_service.dto.reset.VerifyResetTokenResponse;
import com.watchnext.auth_service.dto.social.SocialLoginRequest;
import com.watchnext.auth_service.enums.AuthProvider;
import com.watchnext.auth_service.service.AuthService;
import com.watchnext.auth_service.service.social.SocialAuthService;
import com.watchnext.common.enums.Language;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SocialAuthService socialAuthService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(
        @RequestParam(defaultValue = "ES") Language language,
        @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request, language);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/confirm-email")
    public ResponseEntity<Void> confirmEmail(
        @Valid @RequestBody ConfirmEmailRequest request
    ) {
        authService.confirmEmail(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/resend-code")
    public ResponseEntity<Void> resendCode(
        @RequestParam(defaultValue = "ES") Language language,
        @Valid @RequestBody ResendCodeRequest request
    ) {
        authService.resendCode(request, language);
        return ResponseEntity.status(HttpStatus.OK).build();
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

    @PostMapping("/oauth/{provider}")
    public ResponseEntity<AuthResponse> oauthLogin(
        @PathVariable AuthProvider provider,
        @Valid @RequestBody SocialLoginRequest request
    ) {
        return ResponseEntity.ok(
            socialAuthService.authenticate(provider, request.token())
        );
    }

    // ---------- Recuperar contraseña ----------

    @PostMapping("/password/request")
    public ResponseEntity<Void> requestPasswordReset(
        @RequestParam(defaultValue = "ES") Language language,
        @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.requestPasswordReset(request, language);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password/verify")
    public ResponseEntity<VerifyResetTokenResponse> verifyResetCode(
        @Valid @RequestBody VerifyResetCodeRequest request
    ) {
        return ResponseEntity.ok(authService.verifyResetCode(request));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(
        @Valid @RequestBody NewPasswordRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}
