package com.watchnext.user_service.dto;

import java.util.UUID;

public record PublicProfileResponse(
    UUID id,
    String username,
    String displayName,
    String avatarUrl,
    int followersCount,
    int followingCount,
    boolean privateProfile
) {}
