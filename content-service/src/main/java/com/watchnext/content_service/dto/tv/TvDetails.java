package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchnext.content_service.dto.common.AlternativeTitle;
import com.watchnext.content_service.dto.common.CastMember;
import com.watchnext.content_service.dto.common.CrewMember;
import com.watchnext.content_service.dto.common.ExternalIds;
import com.watchnext.content_service.dto.common.Genre;
import com.watchnext.content_service.dto.common.MediaSummary;
import com.watchnext.content_service.dto.common.Video;
import java.util.List;

public record TvDetails(
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
    List<CastMember> cast,
    List<Video> videos,
    @JsonProperty("created_by") List<CrewMember> createdBy,
    @JsonProperty("external_ids") ExternalIds externalIds,
    List<Genre> keywords,
    @JsonProperty("alternative_titles") List<AlternativeTitle> alternativeTitles,
    List<MediaSummary> recommendations,
    List<MediaSummary> similar
) {}
