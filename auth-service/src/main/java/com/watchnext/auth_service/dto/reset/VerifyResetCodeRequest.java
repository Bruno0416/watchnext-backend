package com.watchnext.auth_service.dto.reset;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyResetCodeRequest(
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ser un correo válido: email@example.com")
    String email,
    @NotBlank(message = "El código es obligatorio") String code
) {}
