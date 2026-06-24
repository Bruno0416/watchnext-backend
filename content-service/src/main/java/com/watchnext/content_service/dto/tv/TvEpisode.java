package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TvEpisode(
    Integer id,
    String name,
    String overview,
    @JsonProperty("episode_number") Integer episodeNumber,
    @JsonProperty("season_number") Integer seasonNumber,
    @JsonProperty("still_path") String stillPath,
    @JsonProperty("vote_average") Double voteAverage,
    @JsonProperty("air_date") String airDate,
    Integer runtime
) {}
