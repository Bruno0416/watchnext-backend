package com.watchnext.content_service.dto.movies;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieListResponse {

    private Integer page;

    private List<MovieSummary> results;

    @JsonProperty("total_pages")
    private Integer totalPages;

    @JsonProperty("total_results")
    private Integer totalResults;

    private Dates dates;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dates {

        private String maximum;

        private String minimum;
    }
}
