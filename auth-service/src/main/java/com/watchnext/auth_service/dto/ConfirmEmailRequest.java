package com.watchnext.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmEmailRequest(
    @NotBlank(message = "El correo no puede estar vacío") String email,
    @NotBlank(message = "El código no puede estar vacío") String code
) {}
