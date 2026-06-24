package com.watchnext.content_service.dto.search;

public record SearchResult(
    Long tmdbId,
    String title,
    String overview,
    String posterPath,
    String mediaType,
    Integer year,
    double popularity,
    double voteAverage
) {}
