package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonAlias;

public record TvSeason(
    Integer id,
    String name,
    String overview,
    @JsonAlias("season_number") Integer seasonNumber,
    @JsonAlias("episode_count") Integer episodeCount,
    @JsonAlias("poster_path") String posterPath
) {}
