package com.watchnext.common.dto;

import com.watchnext.common.model.MediaType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

@Value
public class ContentRefRequest {

    @Positive(message = "El tmdbId debe ser un número positivo")
    @NotNull(message = "El tmdbId no puede ser nulo")
    Long tmdbId;

    @NotNull(message = "El mediaType no puede ser nulo")
    MediaType mediaType;
}
