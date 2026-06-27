package com.watchnext.feedback_service.dto.review;

import jakarta.validation.constraints.NotBlank;

public record ReviewUpdateRequest(
    @NotBlank(message = "El cuerpo de la reseña es necesario") String body
) {}
