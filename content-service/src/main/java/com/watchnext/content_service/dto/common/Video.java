package com.watchnext.content_service.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Video(
    String id,
    String key,
    String site,
    String type,
    String name,
    @JsonProperty("official") boolean official
) {}
