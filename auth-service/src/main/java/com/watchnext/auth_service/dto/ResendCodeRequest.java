package com.watchnext.auth_service.dto;

import com.watchnext.common.enums.CodeType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResendCodeRequest(
    @NotBlank(message = "El correo no puede estar vacío")
    @Email(message = "Debe ser un correo válido: email@example.com")
    String email,

    @NotNull(message = "El tipo de código no puede estar vacío") CodeType type
) {}
