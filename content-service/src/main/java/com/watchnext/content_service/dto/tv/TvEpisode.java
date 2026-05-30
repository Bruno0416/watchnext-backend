package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TvEpisode {

    private Integer id;
    private String name; // titulo del episodio
    private String overview; // resumen del episodio

    @JsonProperty("episode_number")
    private Integer episodeNumber;

    @JsonProperty("season_number")
    private Integer seasonNumber;

    @JsonProperty("still_path")
    private String stillPath; // miniatura episodio

    @JsonProperty("vote_average")
    private Double voteAverage; // rating

    @JsonProperty("air_date")
    private String airDate;

    private Integer runtime;
}
