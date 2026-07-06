package com.watchnext.user_service.dto;

import com.watchnext.common.enums.MediaType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FavoriteItemRequest(
    @Positive(message = "El tmdbId debe ser un número positivo")
    @NotNull(message = "El tmdbId no puede ser nulo")
    Integer tmdbId,

    @NotNull(message = "El mediaType no puede ser nulo")
    MediaType mediaType,

    @Min(value = 0, message = "La posición debe ser 0 o mayor")
    Integer position
) {}
