package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Summary of a TV season as returned inside TvDetails.seasons[].
 * Episodes are NOT included here to avoid N+1 calls to TMDB.
 * Use GET /api/v1/content/tv/{id}/season/{n} to get the full episode list.
 */
public record TvSeason(
    Integer id,
    String name,
    String overview,
    @JsonAlias("season_number") Integer seasonNumber,
    @JsonAlias("episode_count") Integer episodeCount,
    @JsonAlias("poster_path") String posterPath
) {}
