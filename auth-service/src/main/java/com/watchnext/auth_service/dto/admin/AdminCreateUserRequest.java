package com.watchnext.auth_service.dto.admin;

import com.watchnext.auth_service.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// alta de cuenta desde el panel admin, mismas reglas de clave que el registro
public record AdminCreateUserRequest(
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe ser un correo válido: email@example.com")
    @Size(max = 100, message = "El correo no puede superar 100 caracteres")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 60, message = "La contraseña debe tener entre 8 y 60 caracteres.")
    String password,

    @NotNull(message = "El rol es obligatorio")
    Role role
) {}
