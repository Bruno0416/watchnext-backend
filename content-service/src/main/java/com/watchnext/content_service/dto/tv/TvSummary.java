package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TvSummary(
    Integer id,
    String name,
    @JsonProperty("original_name") String originalName,
    String overview,
    @JsonProperty("poster_path") String posterPath,
    @JsonProperty("backdrop_path") String backdropPath,
    @JsonProperty("first_air_date") String firstAirDate,
    @JsonProperty("vote_average") Double voteAverage,
    @JsonProperty("vote_count") Integer voteCount,
    @JsonProperty("genre_ids") List<Integer> genreIds
) {}
