package com.watchnext.common.dto;

import com.watchnext.common.enums.MediaType;

public record ContentRefResponse(Integer tmdbId, MediaType mediaType) {}
