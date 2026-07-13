package com.watchnext.feedback_service.dto.aggregated;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TmdbReviewPage(
    Integer page,
    List<TmdbReviewRaw> results,
    @JsonProperty("total_pages") Integer totalPages,
    @JsonProperty("total_results") Integer totalResults
) {}