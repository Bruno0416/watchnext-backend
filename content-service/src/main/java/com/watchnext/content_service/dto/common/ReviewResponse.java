package com.watchnext.content_service.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ReviewResponse(
    Integer page,
    List<Review> results,
    @JsonProperty("total_pages") Integer totalPages,
    @JsonProperty("total_results") Integer totalResults
) {}
