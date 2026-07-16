package com.watchnext.email_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchnext.common.dto.internal.ConfirmationEmailRequest;
import com.watchnext.common.dto.internal.RecoveryEmailRequest;
import com.watchnext.common.exceptions.GlobalExceptionHandler;
import com.watchnext.common.exceptions.RateLimitExceeded;
import com.watchnext.email_service.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmailController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva filtro JWT y seguridad para ejecutar el test
@Import(GlobalExceptionHandler.class)
public class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    // -------------- SEND CONFIRMATION --------------

    // Codigo 200
    @Test
    public void testSendConfirmation() throws Exception {
        // 1. preparar request
        ConfirmationEmailRequest request = new ConfirmationEmailRequest("test@example.com", "123456");

        // 2. ejecutar test
        doNothing().when(emailService).buildAndSendEmail(any(), any(), any(), any());

        mockMvc
            .perform(
                post("/api/v1/email/internal/send-confirmation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());
    }

    // Codigo 400
    @Test
    public void testSendConfirmationInvalidFields() throws Exception {
        // 1. preparar request (email invalido)
        ConfirmationEmailRequest request = new ConfirmationEmailRequest("not-an-email", "123456");

        // 2. ejecutar test
        mockMvc
            .perform(
                post("/api/v1/email/internal/send-confirmation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    // Codigo 429 - limite de envios excedido
    @Test
    public void testSendConfirmationRateLimited() throws Exception {
        // 1. preparar request
        ConfirmationEmailRequest request = new ConfirmationEmailRequest("test@example.com", "123456");

        // 2. simular que el servicio detecta el limite de envios excedido
        doThrow(new RateLimitExceeded("Too many requests, please wait before requesting another code", 60))
            .when(emailService)
            .buildAndSendEmail(any(), any(), any(), any());

        // 3. verificar codigo 429 y header Retry-After
        mockMvc
            .perform(
                post("/api/v1/email/internal/send-confirmation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isTooManyRequests())
            .andExpect(header().string("Retry-After", "60"));
    }

    // -------------- SEND PASSWORD RECOVERY --------------

    // Codigo 200
    @Test
    public void testSendPasswordRecovery() throws Exception {
        // 1. preparar request
        RecoveryEmailRequest request = new RecoveryEmailRequest("test@example.com", "123456");

        // 2. ejecutar test
        doNothing().when(emailService).buildAndSendEmail(any(), any(), any(), any());

        mockMvc
            .perform(
                post("/api/v1/email/internal/send-password-recovery")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());
    }
}
