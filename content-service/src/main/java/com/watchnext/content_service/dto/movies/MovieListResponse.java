package com.watchnext.content_service.dto.movies;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MovieListResponse(
    Integer page,
    List<MovieSummary> results,
    @JsonProperty("total_pages") Integer totalPages,
    @JsonProperty("total_results") Integer totalResults,
    Dates dates
) {
    public record Dates(String maximum, String minimum) {}
}
