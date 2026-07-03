package com.watchnext.content_service.dto.movies;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchnext.content_service.dto.common.Credits;
import com.watchnext.content_service.dto.common.Genre;
import com.watchnext.content_service.dto.common.VideoWrapper;
import java.util.List;

public record MovieDetailsRaw(
    Integer id,
    String title,
    @JsonProperty("original_title") String originalTitle,
    String overview,
    @JsonProperty("poster_path") String posterPath,
    @JsonProperty("backdrop_path") String backdropPath,
    @JsonProperty("release_date") String releaseDate,
    Integer runtime,
    @JsonProperty("vote_average") Double voteAverage,
    @JsonProperty("vote_count") Integer voteCount,
    List<Genre> genres,
    Credits credits,
    VideoWrapper videos
) {}
