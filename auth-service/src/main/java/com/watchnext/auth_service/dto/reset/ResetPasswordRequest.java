package com.watchnext.auth_service.dto.reset;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ser un correo valido: email@example.com")
    String email
) {}
