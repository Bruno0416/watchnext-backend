package com.watchnext.content_service.dto.tv;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TvSeason {

    private Integer id;
    private String name;
    private String overview;

    @JsonProperty("season_number")
    private Integer seasonNumber;

    @JsonProperty("episode_count")
    private Integer episodeCount;

    @JsonProperty("poster_path")
    private String posterPath;

    private List<TvEpisode> episodes;
}
