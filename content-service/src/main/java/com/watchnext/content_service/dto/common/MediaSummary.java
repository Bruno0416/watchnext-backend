package com.watchnext.content_service.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MediaSummary(
    Long id,
    String title,
    @JsonProperty("poster_path") String posterPath,
    @JsonProperty("vote_average") Double voteAverage,
    @JsonProperty("media_type") String mediaType,
    @JsonProperty("release_date") String releaseDate
) {}
