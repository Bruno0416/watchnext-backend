package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public record TvSeasonDetail(
    Integer id,
    String name,
    String overview,
    @JsonAlias("season_number") Integer seasonNumber,
    @JsonAlias("poster_path") String posterPath,
    List<TvEpisode> episodes,
    @JsonAlias("season_average") Double seasonAverage
) {}
