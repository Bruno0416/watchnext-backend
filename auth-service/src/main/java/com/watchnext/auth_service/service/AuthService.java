package com.watchnext.auth_service.service;

import com.watchnext.auth_service.dto.AuthResponse;
import com.watchnext.auth_service.dto.ConfirmEmailRequest;
import com.watchnext.auth_service.dto.LoginRequest;
import com.watchnext.auth_service.dto.RegisterRequest;
import com.watchnext.auth_service.dto.ResendCodeRequest;
import com.watchnext.auth_service.dto.reset.NewPasswordRequest;
import com.watchnext.auth_service.dto.reset.ResetPasswordRequest;
import com.watchnext.auth_service.dto.reset.VerifyResetCodeRequest;
import com.watchnext.auth_service.dto.reset.VerifyResetTokenResponse;
import com.watchnext.common.enums.Language;

public interface AuthService {
    // ---------- Register ----------
    void register(RegisterRequest request, Language language);

    void resendCode(ResendCodeRequest request, Language language);

    void confirmEmail(ConfirmEmailRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    // ---------- Recuperar contraseña 3 pasos ----------

    // 1. solicitar el correo para resetear la contraseña
    void requestPasswordReset(ResetPasswordRequest request, Language language);

    // 2. validar que el codigo es valido
    VerifyResetTokenResponse verifyResetCode(VerifyResetCodeRequest request);

    // 3. resetear la contraseña
    void resetPassword(NewPasswordRequest request);
}
