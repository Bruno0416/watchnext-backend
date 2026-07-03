package com.watchnext.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ser un correo valido: email@example.com")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(
        min = 8,
        max = 60,
        message = "La contraseña debe tener al menos 8 caracteres."
    )
    String password
) {}
