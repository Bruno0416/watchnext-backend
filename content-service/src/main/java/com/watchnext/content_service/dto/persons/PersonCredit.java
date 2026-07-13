package com.watchnext.content_service.dto.persons;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PersonCredit(
    Long id,
    String title,
    @JsonProperty("poster_path") String posterPath,
    @JsonProperty("media_type") String mediaType,
    String character,
    String department,
    @JsonProperty("vote_average") Double voteAverage,
    @JsonProperty("release_date") String releaseDate,
    Double popularity
) {}
