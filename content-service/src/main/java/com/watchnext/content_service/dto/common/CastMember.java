package com.watchnext.content_service.dto.common;

import com.fasterxml.jackson.annotation.JsonAlias;

public record CastMember(
    Long id,
    String name,
    String character,
    @JsonAlias("profile_path") String profilePath,
    int order
) {}
