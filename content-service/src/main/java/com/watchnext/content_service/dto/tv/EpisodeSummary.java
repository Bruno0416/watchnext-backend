package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Reduced episode representation used in the season episode listing.
 * For a full episode detail (airDate, runtime, voteAverage…) see TvEpisode.
 */
public record EpisodeSummary(
    Long id,
    String name,
    String overview,
    @JsonAlias("episode_number") int episodeNumber,
    @JsonAlias("still_path") String stillPath
) {}
