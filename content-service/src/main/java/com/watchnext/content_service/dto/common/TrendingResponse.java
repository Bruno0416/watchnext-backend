package com.watchnext.content_service.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TrendingResponse(
    Integer page,
    List<TrendingItem> results,
    @JsonProperty("total_pages") Integer totalPages,
    @JsonProperty("total_results") Integer totalResults
) {}
