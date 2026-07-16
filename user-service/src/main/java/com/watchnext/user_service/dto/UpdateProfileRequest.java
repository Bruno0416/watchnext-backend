package com.watchnext.user_service.dto;

import com.watchnext.user_service.enums.ProfileVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateProfileRequest(
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    String displayName,

    @Size(max = 500, message = "La bio no puede exceder 500 caracteres")
    String bio,

    ProfileVisibility visibility,

    @Pattern(
        regexp = "^[A-Za-z]{2}$",
        message = "El país debe ser un código ISO 3166-1 alfa-2 de 2 letras"
    )
    String country,

    List<@Valid FavoriteItemRequest> favorites
) {}
