package com.watchnext.feedback_service.dto.aggregated;

import java.time.Instant;
import java.util.UUID;

public record WatchNextReviewItem(
    UUID id,
    String userId,
    String body,
    Instant createdAt,
    ReviewSource source
) {}