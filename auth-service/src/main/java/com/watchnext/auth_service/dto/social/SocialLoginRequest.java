package com.watchnext.auth_service.dto.social;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(
    @NotBlank(message = "El token no puede estar vacio") String token
) {}
