package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchnext.content_service.dto.common.Credits;
import com.watchnext.content_service.dto.common.Genre;
import com.watchnext.content_service.dto.common.VideoWrapper;
import java.util.List;

public record TvDetailsRaw(
    Integer id,
    String name,
    String overview,
    @JsonProperty("poster_path") String posterPath,
    @JsonProperty("backdrop_path") String backdropPath,
    @JsonProperty("first_air_date") String firstAirDate,
    @JsonProperty("vote_average") Double voteAverage,
    @JsonProperty("number_of_episodes") Integer numberOfEpisodes,
    @JsonProperty("number_of_seasons") Integer numberOfSeasons,
    String status,
    List<Genre> genres,
    List<TvSeason> seasons,
    Credits credits,
    VideoWrapper videos
) {}
