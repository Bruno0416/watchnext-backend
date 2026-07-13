package com.watchnext.auth_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchnext.auth_service.dto.AuthResponse;
import com.watchnext.auth_service.dto.LoginRequest;
import com.watchnext.auth_service.dto.RefreshTokenRequest;
import com.watchnext.auth_service.dto.RegisterRequest;
import com.watchnext.auth_service.dto.social.SocialLoginRequest;
import com.watchnext.auth_service.enums.AuthProvider;
import com.watchnext.auth_service.exceptions.EmailAlreadyInUse;
import com.watchnext.auth_service.exceptions.EmailNotVerified;
import com.watchnext.auth_service.exceptions.InvalidCredentials;
import com.watchnext.auth_service.exceptions.InvalidRefreshToken;
import com.watchnext.auth_service.exceptions.InvalidSocialToken;
import com.watchnext.auth_service.exceptions.SocialOnlyAccount;
import com.watchnext.auth_service.exceptions.UnsupportedProvider;
import com.watchnext.auth_service.repository.UserRepository;
import com.watchnext.auth_service.service.AuthService;
import com.watchnext.auth_service.service.social.SocialAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
    com.watchnext.common.config.converters.StringToEnumConverterFactory.class,
    com.watchnext.common.exceptions.GlobalExceptionHandler.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private SocialAuthService socialAuthService;

    // -------------- 1. REGISTER --------------

    @Test
    void testRegister() throws Exception {
        // 1. preparar request
        var request = new RegisterRequest("user@example.com", "password123");
        doNothing().when(authService).register(any(), any());

        // 2. ejecutar y verificar
        // 201
        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void testRegisterEmptyEmail() throws Exception {
        // 1. preparar request (email vacío)
        var request = new RegisterRequest("", "password123");

        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterInvalidEmail() throws Exception {
        // 1. preparar request (email sin formato válido)
        var request = new RegisterRequest("not-an-email", "password123");

        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterInvalidPassword() throws Exception {
        // 1. preparar request (password < 6 chars)
        var request = new RegisterRequest("user@example.com", "abc");

        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterConflict() throws Exception {
        // 1. preparar request
        var request = new RegisterRequest("user@example.com", "password123");
        doThrow(new EmailAlreadyInUse("user@example.com"))
            .when(authService)
            .register(any(), any());

        // 2. ejecutar y verificar
        // 409
        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isConflict());
    }

    // -------------- 2. LOGIN --------------

    @Test
    void testLogin() throws Exception {
        // 1. preparar request y respuesta esperada
        var request = new LoginRequest("user@example.com", "password123");
        var response = new AuthResponse("jwt-token", "refresh-token");
        when(authService.login(any())).thenReturn(response);

        // 2. ejecutar y verificar
        // 200
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void testLoginEmptyEmail() throws Exception {
        // 1. preparar request (email vacío)
        var request = new LoginRequest("", "password123");

        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginInvlidPassword() throws Exception {
        // 1. preparar request (password vacía)
        var request = new LoginRequest("user@example.com", "");

        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        // 1. preparar request
        var request = new LoginRequest("user@example.com", "wrong-password");
        when(authService.login(any())).thenThrow(
            new InvalidCredentials("Credenciales inválidas")
        );

        // 2. ejecutar y verificar
        // 401
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginSocialOnlyAccount() throws Exception {
        // 1. preparar request para una cuenta creada via login social (sin password local)
        var request = new LoginRequest("social@example.com", "any-password");
        when(authService.login(any())).thenThrow(new SocialOnlyAccount());

        // 2. ejecutar y verificar
        // 409
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isConflict());
    }

    // -------------- 3. REFRESH --------------

    @Test
    void testRefresh() throws Exception {
        // 1. preparar request y respuesta esperada
        var request = new RefreshTokenRequest("valid-refresh-token");
        var response = new AuthResponse("new-jwt-token", "new-refresh-token");
        when(authService.refreshToken(anyString())).thenReturn(response);

        // 2. ejecutar y verificar
        // 200
        mockMvc
            .perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("new-jwt-token"))
            .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void testRefreshInvalidToken() throws Exception {
        // 1. preparar request
        var request = new RefreshTokenRequest("invalid-refresh-token");
        when(authService.refreshToken(anyString())).thenThrow(
            new InvalidRefreshToken("Token inválido")
        );

        // 2. ejecutar y verificar
        // 401
        mockMvc
            .perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isUnauthorized());
    }

    // -------------- 4. OAUTH --------------

    @Test
    void testOauthLogin() throws Exception {
        // 1. preparar request y respuesta esperada
        var request = new SocialLoginRequest("valid-google-id-token");
        var response = new AuthResponse("jwt-token", "refresh-token");
        when(
            socialAuthService.authenticate(
                AuthProvider.GOOGLE,
                "valid-google-id-token"
            )
        ).thenReturn(response);

        // 2. ejecutar y verificar
        // 200
        mockMvc
            .perform(
                post("/api/v1/auth/oauth/google")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void testOauthLoginProviderCaseInsensitive() throws Exception {
        // 1. preparar request y respuesta esperada
        var request = new SocialLoginRequest("valid-google-id-token");
        var response = new AuthResponse("jwt-token", "refresh-token");
        when(
            socialAuthService.authenticate(
                AuthProvider.GOOGLE,
                "valid-google-id-token"
            )
        ).thenReturn(response);

        // 2. ejecutar y verificar (provider en mayusculas)
        // 200
        mockMvc
            .perform(
                post("/api/v1/auth/oauth/GOOGLE")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());
    }

    @Test
    void testOauthLoginUnknownProvider() throws Exception {
        // 1. preparar request con un provider que no existe en el enum
        var request = new SocialLoginRequest("some-token");

        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                post("/api/v1/auth/oauth/bogus")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testOauthLoginBlankToken() throws Exception {
        // 1. preparar request con token vacio
        var request = new SocialLoginRequest("");

        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                post("/api/v1/auth/oauth/google")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testOauthLoginInvalidToken() throws Exception {
        // 1. preparar request
        var request = new SocialLoginRequest("bad-token");
        when(socialAuthService.authenticate(any(), any())).thenThrow(
            new InvalidSocialToken("Token de Google invalido o expirado")
        );

        // 2. ejecutar y verificar
        // 401
        mockMvc
            .perform(
                post("/api/v1/auth/oauth/google")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testOauthLoginUnsupportedProviderFromService() throws Exception {
        // 1. preparar request (provider valido en el enum, pero sin verifier registrado)
        var request = new SocialLoginRequest("some-token");
        when(socialAuthService.authenticate(any(), any())).thenThrow(
            new UnsupportedProvider("FACEBOOK")
        );

        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                post("/api/v1/auth/oauth/facebook")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testOauthLoginEmailNotVerified() throws Exception {
        // 1. preparar request
        var request = new SocialLoginRequest("valid-token-unverified-email");
        when(socialAuthService.authenticate(any(), any())).thenThrow(
            new EmailNotVerified("user@example.com")
        );

        // 2. ejecutar y verificar
        // 409
        mockMvc
            .perform(
                post("/api/v1/auth/oauth/google")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isConflict());
    }
}
