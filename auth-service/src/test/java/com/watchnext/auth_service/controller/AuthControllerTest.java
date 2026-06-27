package com.watchnext.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchnext.auth_service.dto.AuthResponse;
import com.watchnext.auth_service.dto.LoginRequest;
import com.watchnext.auth_service.dto.RefreshTokenRequest;
import com.watchnext.auth_service.dto.RegisterRequest;
import com.watchnext.auth_service.exceptions.EmailAlreadyInUse;
import com.watchnext.auth_service.exceptions.InvalidCredentials;
import com.watchnext.auth_service.exceptions.InvalidRefreshToken;
import com.watchnext.auth_service.repository.UserRepository;
import com.watchnext.auth_service.service.AuthService;
import com.watchnext.common.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    // -------------- 1. REGISTER --------------

    @Test
    void testRegister_success() throws Exception {
        // 1. preparar request
        var request = new RegisterRequest("user@example.com", "password123", "username");
        doNothing().when(authService).register(any());

        // 2. ejecutar y verificar
        // 201
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void testRegister_emailEnBlanco_400() throws Exception {
        // 1. preparar request (email vacío)
        var request = new RegisterRequest("", "password123", "username");

        // 2. ejecutar y verificar
        // 400
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_emailInvalido_400() throws Exception {
        // 1. preparar request (email sin formato válido)
        var request = new RegisterRequest("not-an-email", "password123", "username");

        // 2. ejecutar y verificar
        // 400
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_passwordCorta_400() throws Exception {
        // 1. preparar request (password < 6 chars)
        var request = new RegisterRequest("user@example.com", "abc", "username");

        // 2. ejecutar y verificar
        // 400
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_usernameEnBlanco_400() throws Exception {
        // 1. preparar request (username vacío)
        var request = new RegisterRequest("user@example.com", "password123", "");

        // 2. ejecutar y verificar
        // 400
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_emailYaEnUso_409() throws Exception {
        // 1. preparar request
        var request = new RegisterRequest("user@example.com", "password123", "username");
        doThrow(new EmailAlreadyInUse("user@example.com")).when(authService).register(any());

        // 2. ejecutar y verificar
        // 409
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    // -------------- 2. LOGIN --------------

    @Test
    void testLogin_success() throws Exception {
        // 1. preparar request y respuesta esperada
        var request = new LoginRequest("user@example.com", "password123");
        var response = new AuthResponse("jwt-token", "refresh-token");
        when(authService.login(any())).thenReturn(response);

        // 2. ejecutar y verificar
        // 200
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void testLogin_emailEnBlanco_400() throws Exception {
        // 1. preparar request (email vacío)
        var request = new LoginRequest("", "password123");

        // 2. ejecutar y verificar
        // 400
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_passwordEnBlanco_400() throws Exception {
        // 1. preparar request (password vacía)
        var request = new LoginRequest("user@example.com", "");

        // 2. ejecutar y verificar
        // 400
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_credencialesInvalidas_401() throws Exception {
        // 1. preparar request
        var request = new LoginRequest("user@example.com", "wrong-password");
        when(authService.login(any())).thenThrow(new InvalidCredentials("Credenciales inválidas"));

        // 2. ejecutar y verificar
        // 401
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    // -------------- 3. REFRESH --------------

    @Test
    void testRefresh_success() throws Exception {
        // 1. preparar request y respuesta esperada
        var request = new RefreshTokenRequest("valid-refresh-token");
        var response = new AuthResponse("new-jwt-token", "new-refresh-token");
        when(authService.refreshToken(anyString())).thenReturn(response);

        // 2. ejecutar y verificar
        // 200
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("new-jwt-token"))
            .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void testRefresh_tokenInvalido_401() throws Exception {
        // 1. preparar request
        var request = new RefreshTokenRequest("invalid-refresh-token");
        when(authService.refreshToken(anyString())).thenThrow(new InvalidRefreshToken("Token inválido"));

        // 2. ejecutar y verificar
        // 401
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
}
