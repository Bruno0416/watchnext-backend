package com.watchnext.feedback_service.dto.review;

import com.watchnext.common.model.MediaType;
import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
    UUID id,
    String userId,
    Integer tmdbId,
    MediaType mediaType,
    String body,
    Instant createdAt,
    Instant updatedAt
) {}
