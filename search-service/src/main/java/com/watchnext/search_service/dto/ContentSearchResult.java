package com.watchnext.search_service.dto;

public record ContentSearchResult(
    Long tmdbId,
    String title,
    String overview,
    String posterPath,
    String mediaType,
    Integer year,
    double popularity,
    double voteAverage,
    String knownForDepartment
) {}
