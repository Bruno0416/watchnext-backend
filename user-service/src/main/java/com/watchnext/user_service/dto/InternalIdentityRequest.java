package com.watchnext.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// mismas reglas de identidad que el onboarding
public record InternalIdentityRequest(
    @NotBlank(message = "El username es obligatorio")
    @Pattern(
        regexp = "^[a-z0-9_]{3,30}$",
        message = "El usuario debe tener entre 3 y 30 caracteres: letras minusculas, numeros o guion bajo."
    )
    String username,

    @Size(max = 50, message = "El nombre visible no puede superar 50 caracteres.")
    String displayName
) {}
