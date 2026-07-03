package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonAlias;

public record EpisodeSummary(
    Long id,
    String name,
    String overview,
    @JsonAlias("episode_number") int episodeNumber,
    @JsonAlias("still_path") String stillPath
) {}
