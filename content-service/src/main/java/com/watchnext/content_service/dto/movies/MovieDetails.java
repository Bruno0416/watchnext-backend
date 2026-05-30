package com.watchnext.content_service.dto.movies;

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
public class MovieDetails {

    private Integer id;

    private String title;

    @JsonProperty("original_title")
    private String originalTitle;

    private String overview;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("release_date")
    private String releaseDate;

    private Integer runtime;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("vote_count")
    private Integer voteCount;

    private List<Genre> genres;
}
