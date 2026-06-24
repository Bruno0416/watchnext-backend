package com.watchnext.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank
    @Email(message = "Debe ser un correo valido: email@example.com")
    String email,

    @NotBlank
    @Size(min = 6, max = 60, message = "La contrasenia debe tener al menos 6 caracteres.")
    String password,

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    String username
) {}
