package com.watchnext.user_service.dto;


import com.watchnext.user_service.enums.ProfileVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OnboardingRequest(
    @NotBlank(message = "El username es obligatorio")
    @Pattern(
        regexp = "^[a-z0-9_]{3,30}$",
        message = "Solo letras minúsculas, números y guión bajo (3-30 caracteres)"
    )
    String username,

    @NotNull(message = "La visibilidad es obligatoria")
    ProfileVisibility visibility,

    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    String displayName,

    @Size(max = 500, message = "La bio no puede exceder 500 caracteres")
    String bio,

    @NotNull(message = "El país es obligatorio")
    @Pattern(
        regexp = "^[A-Za-z]{2}$",
        message = "El país debe ser un código ISO 3166-1 alfa-2 de 2 letras"
    )
    String country,

    @Size(max = 5, message = "Máximo 5 favoritos")
    List<@Valid FavoriteItemRequest> favorites
) {
    public OnboardingRequest {
        if (favorites == null) favorites = List.of();
    }
}
