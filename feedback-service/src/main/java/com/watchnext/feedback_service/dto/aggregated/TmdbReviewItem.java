package com.watchnext.feedback_service.dto.aggregated;

import java.time.Instant;

public record TmdbReviewItem(
    String id,
    String author,
    String content,
    Instant createdAt,
    Double rating,
    ReviewSource source
) {}