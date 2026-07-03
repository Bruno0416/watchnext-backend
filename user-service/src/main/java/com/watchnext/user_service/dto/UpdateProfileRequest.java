package com.watchnext.user_service.dto;

import com.watchnext.user_service.enums.ProfileVisibility;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    String displayName,

    @Size(max = 500, message = "La bio no puede exceder 500 caracteres")
    String bio,

    ProfileVisibility visibility
) {}
