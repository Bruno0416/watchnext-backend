package com.watchnext.content_service.dto.common;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Represents a cast member from TMDB credits.cast[].
 */
public record CastMember(
    Long id,
    String name,
    String character,
    @JsonAlias("profile_path") String profilePath,
    int order
) {}
