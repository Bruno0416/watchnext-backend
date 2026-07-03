package com.watchnext.common.dto.internal;

import com.watchnext.common.enums.MediaType;

public record ContentBasicDetail(
    Integer tmdbId,
    MediaType mediaType,
    String title,
    String posterPath,
    Double voteAverage,
    String releaseDate,
    Integer duration,
    Integer numberOfSeasons
) {}
