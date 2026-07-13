package com.watchnext.feedback_service.dto.aggregated;

import java.util.List;

public record TmdbReviewsSection(
    List<TmdbReviewItem> reviews,
    int page,
    int totalPages,
    long totalElements,
    boolean available
) {}