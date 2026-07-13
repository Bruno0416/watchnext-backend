package com.watchnext.feedback_service.dto.aggregated;

public record AggregatedReviewsResponse(
    WatchNextReviewsSection watchnext,
    TmdbReviewsSection tmdb
) {}