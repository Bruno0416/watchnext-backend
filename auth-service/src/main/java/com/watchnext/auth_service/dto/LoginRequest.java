package com.watchnext.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "El correo no puede estar vacio")
    @Email(message = "Debe ser un correo valido: email@example.com")
    private String email;

    @NotBlank(message = "La contrasenia no puede estar vacia")
    private String password;
}
