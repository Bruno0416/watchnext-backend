package com.watchnext.auth_service.service;

import com.watchnext.auth_service.client.EmailServiceClient;
import com.watchnext.auth_service.dto.AuthResponse;
import com.watchnext.auth_service.dto.ConfirmEmailRequest;
import com.watchnext.auth_service.dto.LoginRequest;
import com.watchnext.auth_service.dto.RegisterRequest;
import com.watchnext.auth_service.dto.ResendCodeRequest;
import com.watchnext.auth_service.dto.reset.NewPasswordRequest;
import com.watchnext.auth_service.dto.reset.ResetPasswordRequest;
import com.watchnext.auth_service.dto.reset.VerifyResetCodeRequest;
import com.watchnext.auth_service.dto.reset.VerifyResetTokenResponse;
import com.watchnext.auth_service.entity.Users;
import com.watchnext.auth_service.enums.Role;
import com.watchnext.auth_service.exceptions.EmailAlreadyInUse;
import com.watchnext.auth_service.exceptions.InvalidConfirmationCode;
import com.watchnext.auth_service.exceptions.InvalidCredentials;
import com.watchnext.auth_service.exceptions.InvalidRefreshToken;
import com.watchnext.auth_service.exceptions.SocialOnlyAccount;
import com.watchnext.auth_service.exceptions.UserNotFound;
import com.watchnext.auth_service.repository.UserRepository;
import com.watchnext.auth_service.security.JwtUtil;
import com.watchnext.auth_service.service.code.VerificationCodeService;
import com.watchnext.common.enums.CodeType;
import com.watchnext.common.enums.Language;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final VerificationCodeService verifCodeService;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;

    private final EmailServiceClient emailServiceClient;

    // --- Helper privado ---
    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public void register(RegisterRequest request, Language language) {
        String email = normalizeEmail(request.email());

        // 1. validar si el usuario ya tiene cuenta
        if (userRepo.existsByEmail(email)) {
            throw new EmailAlreadyInUse(email);
        }

        // 2. registrar usuario
        userRepo.save(
            Users.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build()
        );

        // 3. generar y enviar codigo de confirmación por email
        String confirmationCode = verifCodeService.generateCode(
            email,
            CodeType.CONFIRMATION
        );

        emailServiceClient.sendConfirmation(email, confirmationCode, language);
    }

    @Override
    public void resendCode(ResendCodeRequest request, Language language) {
        String email = normalizeEmail(request.email());
        String confirmationCode = verifCodeService.generateCode(
            email,
            request.type()
        );

        emailServiceClient.sendConfirmation(email, confirmationCode, language);
    }

    @Override
    @Transactional
    public void confirmEmail(ConfirmEmailRequest request) {
        boolean isValid = verifCodeService.validateCode(
            request.email(),
            CodeType.CONFIRMATION,
            request.code()
        );

        if (!isValid) throw new InvalidConfirmationCode(
            "El código de confirmación es inválido o ha expirado"
        );

        Users user = userRepo
            .findByEmail(request.email())
            .orElseThrow(() -> new UserNotFound(request.email()));

        user.setEmailVerified(true);
        userRepo.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        // 1. buscar usuario primero para detectar cuentas solo-sociales antes de autenticar
        Users user = userRepo
            .findByEmail(email)
            .orElseThrow(() ->
                new InvalidCredentials("Credenciales invalidas")
            );

        // 2. las cuentas creadas via login social no tienen password local
        if (user.getPassword() == null) {
            throw new SocialOnlyAccount();
        }

        // 3. autenticar usuario
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    email,
                    request.password()
                )
            );
        } catch (Exception e) {
            throw new InvalidCredentials("Credenciales invalidas");
        }

        // 4. generar tokens y retornar
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        // 1. extraer el email del token
        String email = jwtUtil.getUsernameFromToken(refreshToken);

        // 2. validar usuario buscando por email extraido
        if (email != null) {
            Users user = userRepo
                .findByEmail(email)
                .orElseThrow(() ->
                    new InvalidCredentials("Credenciales invalidas")
                );

            if (jwtUtil.isTokenValid(refreshToken, user)) {
                String newAccessToken = jwtUtil.generateToken(user);
                String newRefreshToken = jwtUtil.generateRefreshToken(user);

                return new AuthResponse(newAccessToken, newRefreshToken);
            }
        }
        throw new InvalidRefreshToken("RefreshToken invalido o expirado");
    }

    // ---------- Recuperar contraseña 3 pasos ----------

    @Override
    public void requestPasswordReset(
        ResetPasswordRequest request,
        Language language
    ) {
        // 1. normalizar email request
        String email = normalizeEmail(request.email());

        // 2. buscar usuario, si existe generamos y enviamos codigo a su correo
        userRepo.findByEmail(email).ifPresent(user -> {
            String code = verifCodeService.generateCode(
                email,
                CodeType.PASSWORD_RECOVERY
            );
            emailServiceClient.sendPasswordRecovery(email, code, language);
        });
    }

    @Override
    public VerifyResetTokenResponse verifyResetCode(
        VerifyResetCodeRequest request
    ) {
        // 1. normalizar email request
        String normalizedEmail = normalizeEmail(request.email());

        // 2. validar codigo
        boolean isValid = verifCodeService.validateCode(
            normalizedEmail,
            CodeType.PASSWORD_RECOVERY,
            request.code()
        );
        // 2. si no es valido lanzamos excepcion
        if (!isValid) throw new InvalidConfirmationCode(
            "El código de validacion es inválido o ha expirado"
        );

        // 2. si es valido generamos un token temporal para resetear la contraseña
        return new VerifyResetTokenResponse(
            jwtUtil.generateResetToken(normalizedEmail)
        );
    }

    @Override
    public void resetPassword(NewPasswordRequest request) {
        // 1. obtener email desde el token
        String email = jwtUtil.validateResetTokenAndGetEmail(
            request.resetToken()
        );

        // 2. encontrar usuario o arrojar excepcion
        Users user = userRepo
            .findByEmail(email)
            .orElseThrow(() -> new UserNotFound(email));

        // 3. actualizar password y guardar
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepo.save(user);
    }
}
