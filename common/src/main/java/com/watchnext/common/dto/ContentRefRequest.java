package com.watchnext.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchnext.common.model.MediaType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ContentRefRequest(
    @Positive(message = "El tmdbId debe ser un número positivo")
    @NotNull(message = "El tmdbId no puede ser nulo")
    @JsonProperty("tmdbId") Integer tmdbId,

    @NotNull(message = "El mediaType no puede ser nulo")
    @JsonProperty("mediaType") MediaType mediaType
) {
    @JsonCreator
    public ContentRefRequest {}
}
