package com.watchnext.search_service.dto;

import java.util.List;

public record ContentSearchSection(
    String originalQuery,
    String executedQuery,
    boolean corrected,
    int totalResults,
    List<ContentSearchResult> results
) {}
