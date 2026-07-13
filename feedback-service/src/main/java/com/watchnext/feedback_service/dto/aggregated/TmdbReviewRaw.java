package com.watchnext.feedback_service.dto.aggregated;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbReviewRaw(
    String id,
    String author,
    String content,
    @JsonProperty("created_at") String createdAt,
    Double rating
) {}