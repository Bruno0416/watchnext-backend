package com.watchnext.common.dto;

import com.watchnext.common.model.MediaType;

public record ContentRefResponse(
    Integer tmdbId,
    MediaType mediaType
) {}
