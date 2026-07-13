package com.watchnext.feedback_service.service.review;

import com.watchnext.common.enums.MediaType;
import com.watchnext.feedback_service.dto.aggregated.AggregatedReviewsResponse;

public interface AggregatedReviewService {

    AggregatedReviewsResponse getAggregatedReviews(
        MediaType mediaType,
        Integer tmdbId,
        int page,
        int size
    );
}