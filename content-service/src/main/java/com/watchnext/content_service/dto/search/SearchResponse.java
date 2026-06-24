package com.watchnext.content_service.dto.search;

import java.util.List;

public record SearchResponse(
    String originalQuery,
    String executedQuery,
    boolean corrected,
    int totalResults,
    List<SearchResult> results
) {}
