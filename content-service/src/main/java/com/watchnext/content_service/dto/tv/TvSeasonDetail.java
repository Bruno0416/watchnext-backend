package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

/**
 * Full season detail including the episode listing.
 * Returned by GET /api/v1/content/tv/{id}/season/{seasonNumber}.
 */
public record TvSeasonDetail(
    Integer id,
    String name,
    String overview,
    @JsonAlias("season_number") Integer seasonNumber,
    @JsonAlias("poster_path") String posterPath,
    List<EpisodeSummary> episodes
) {}
