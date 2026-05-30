package com.watchnext.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Email(message = "Debe ser un correo valido: email@example.com")
    private String email;

    @NotBlank
    @Size(
        min = 6,
        max = 60,
        message = "La contrasenia debe tener al menos 6 caracteres."
    )
    private String password;

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    private String username;
}
