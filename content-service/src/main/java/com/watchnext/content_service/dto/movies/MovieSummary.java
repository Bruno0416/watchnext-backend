package com.watchnext.content_service.dto.movies;

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
public class MovieSummary {

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

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("vote_count")
    private Integer voteCount;

    private Double popularity;

    @JsonProperty("genre_ids")
    private List<Integer> genreIds;

    @JsonProperty("original_language")
    private String originalLanguage;

    private Boolean adult;

    private Boolean video;
}
