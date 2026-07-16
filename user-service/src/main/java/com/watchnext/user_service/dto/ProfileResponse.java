package com.watchnext.user_service.dto;

import com.watchnext.common.dto.internal.ContentBasicDetail;
import com.watchnext.user_service.enums.ProfileVisibility;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProfileResponse(
    UUID id,
    String userId,
    String username,
    String displayName,
    String bio,
    String avatarUrl,
    String country,
    ProfileVisibility visibility,
    boolean onboardingCompleted,
    int followersCount,
    int followingCount,
    List<ContentBasicDetail> favorites,
    Instant createdAt,
    Instant updatedAt
) {}
