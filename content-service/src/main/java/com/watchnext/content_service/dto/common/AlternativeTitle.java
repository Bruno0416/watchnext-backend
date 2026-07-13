package com.watchnext.content_service.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AlternativeTitle(
    @JsonProperty("iso_3166_1") String iso,
    String title,
    String type
) {}
