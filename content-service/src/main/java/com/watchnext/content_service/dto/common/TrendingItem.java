package com.watchnext.content_service.dto.common;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TrendingItem(
    Long id,
    @JsonAlias("name") String title,
    @JsonProperty("poster_path") String posterPath,
    @JsonProperty("vote_average") Double voteAverage,
    @JsonProperty("media_type") String mediaType,
    @JsonAlias("first_air_date") @JsonProperty("release_date") String releaseDate,
    String overview
) {}
