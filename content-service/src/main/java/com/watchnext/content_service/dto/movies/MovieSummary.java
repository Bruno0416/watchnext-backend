package com.watchnext.content_service.dto.movies;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MovieSummary(
    Integer id,
    String title,
    @JsonProperty("original_title") String originalTitle,
    String overview,
    @JsonProperty("poster_path") String posterPath,
    @JsonProperty("backdrop_path") String backdropPath,
    @JsonProperty("release_date") String releaseDate,
    @JsonProperty("vote_average") Double voteAverage,
    @JsonProperty("vote_count") Integer voteCount,
    Double popularity,
    @JsonProperty("genre_ids") List<Integer> genreIds,
    @JsonProperty("original_language") String originalLanguage,
    Boolean adult,
    Boolean video
) {}
