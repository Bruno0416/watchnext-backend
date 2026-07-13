package com.watchnext.content_service.dto.common;

import com.fasterxml.jackson.annotation.JsonAlias;

public record CrewMember(
    Long id,
    String name,
    String job,
    @JsonAlias("profile_path") String profilePath
) {}
