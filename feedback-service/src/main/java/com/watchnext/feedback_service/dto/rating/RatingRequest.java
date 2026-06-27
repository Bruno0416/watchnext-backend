package com.watchnext.feedback_service.dto.rating;

import com.watchnext.common.model.MediaType;
import jakarta.validation.constraints.NotNull;

public record RatingRequest(
    @NotNull(message = "tmbdId es necesario") Integer tmdbId,
    @NotNull(message = "mediaType es necesario") MediaType mediaType,
    @NotNull(message = "score es necesario") Double score
) {}
