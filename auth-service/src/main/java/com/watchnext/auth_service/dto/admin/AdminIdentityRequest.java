package com.watchnext.auth_service.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// cambio de username y nombre visible, se reenvia a user-service
public record AdminIdentityRequest(
    @NotBlank(message = "El username es obligatorio")
    @Pattern(
        regexp = "^[a-z0-9_]{3,30}$",
        message = "El usuario debe tener entre 3 y 30 caracteres: letras minusculas, numeros o guion bajo."
    )
    String username,

    @Size(max = 50, message = "El nombre visible no puede superar 50 caracteres.")
    String displayName
) {}
