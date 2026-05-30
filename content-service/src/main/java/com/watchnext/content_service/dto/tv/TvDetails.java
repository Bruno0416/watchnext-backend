package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchnext.content_service.dto.common.Genre;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TvDetails {

    private Integer id;
    private String name;
    private String overview;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("first_air_date")
    private String firstAirDate;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("number_of_episodes")
    private Integer numberOfEpisodes;

    @JsonProperty("number_of_seasons")
    private Integer numberOfSeasons;

    private String status;

    private List<Genre> genres;

    private List<TvSeason> seasons;
}
