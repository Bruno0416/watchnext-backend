package com.watchnext.feedback_service.dto.rating;

import com.watchnext.common.model.MediaType;
import java.time.Instant;
import java.util.UUID;

public record RatingResponse(
    UUID id,
    Integer tmdbId,
    MediaType mediaType,
    Double score,
    Instant createdAt,
    Instant updatedAt
) {}
