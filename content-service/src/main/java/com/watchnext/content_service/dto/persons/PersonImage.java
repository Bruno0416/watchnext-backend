package com.watchnext.content_service.dto.persons;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PersonImage(
    @JsonProperty("file_path") String filePath,
    @JsonProperty("vote_average") Double voteAverage
) {}
