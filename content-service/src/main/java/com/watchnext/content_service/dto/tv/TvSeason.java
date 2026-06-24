package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TvSeason(
    Integer id,
    String name,
    String overview,
    @JsonProperty("season_number") Integer seasonNumber,
    @JsonProperty("episode_count") Integer episodeCount,
    @JsonProperty("poster_path") String posterPath,
    List<TvEpisode> episodes
) {}
