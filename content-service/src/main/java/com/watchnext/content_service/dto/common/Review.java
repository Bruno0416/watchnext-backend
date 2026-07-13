package com.watchnext.content_service.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Review(
    String id,
    String author,
    String content,
    @JsonProperty("created_at") String createdAt,
    Double rating
) {}
