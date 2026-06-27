package com.watchnext.content_service.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a video (trailer, teaser, clip…) from TMDB.
 * Only YouTube videos are kept after normalization.
 */
public record Video(
    String id,
    String key,
    String site,
    String type,
    String name,
    @JsonProperty("official") boolean official
) {}
