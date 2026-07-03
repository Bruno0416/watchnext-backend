package com.watchnext.user_service.dto;

public record ProfileSummaryResponse(
    String userId,
    String username,
    String displayName,
    String avatarUrl
) {}
