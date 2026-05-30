package com.watchnext.content_service.dto.tv;

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
public class TvListResponse {

    private Integer page;
    private List<TvSummary> results;

    @JsonProperty("total_pages")
    private Integer totalPages;

    @JsonProperty("total_results")
    private Integer totalResults;
}
