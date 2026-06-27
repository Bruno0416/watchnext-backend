package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchnext.content_service.dto.common.Credits;
import com.watchnext.content_service.dto.common.Genre;
import com.watchnext.content_service.dto.common.VideoWrapper;
import java.util.List;

/**
 * Internal DTO that mirrors the raw TMDB response for
 * GET /tv/{id}?append_to_response=credits,videos.
 *
 * This is NOT what the API exposes. ContentServiceImpl maps this to TvDetails
 * after trimming cast and normalizing videos.
 */
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
