package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchnext.content_service.dto.common.CastMember;
import com.watchnext.content_service.dto.common.CrewMember;
import java.util.List;

public record TvEpisode(
    Integer id,
    String name,
    String overview,
    @JsonProperty("episode_number") Integer episodeNumber,
    @JsonProperty("season_number") Integer seasonNumber,
    @JsonProperty("still_path") String stillPath,
    @JsonProperty("vote_average") Double voteAverage,
    @JsonProperty("vote_count") Integer voteCount,
    @JsonProperty("air_date") String airDate,
    Integer runtime,
    List<CrewMember> crew,
    @JsonProperty("guest_stars") List<CastMember> guestStars
) {}
