package com.watchnext.feedback_service.dto.aggregated;

import java.util.List;

public record WatchNextReviewsSection(
    List<WatchNextReviewItem> reviews,
    int page,
    int totalPages,
    long totalElements
) {}