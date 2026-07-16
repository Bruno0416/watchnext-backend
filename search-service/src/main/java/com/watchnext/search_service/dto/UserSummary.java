package com.watchnext.search_service.dto;

public record UserSummary(
    String userId,
    String username,
    String displayName,
    String avatarUrl
) {}
