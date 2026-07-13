package com.watchnext.content_service.dto.movies;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CollectionDetails(
    Integer id,
    String name,
    String overview,
    @JsonProperty("poster_path") String posterPath,
    @JsonProperty("backdrop_path") String backdropPath,
    List<MovieSummary> parts
) {}
