package com.watchnext.feedback_service.dto.review;

import com.watchnext.common.enums.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
    @NotNull(message = "tmdbId es necesario") Integer tmdbId,
    @NotNull(message = "mediaType es obligatorio") MediaType mediaType,
    @NotBlank(message = "El cuerpo de la reseña es necesario") String body
) {}
