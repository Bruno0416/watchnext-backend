package com.watchnext.user_service.dto;

// datos del perfil que el panel de admin junta con la cuenta de auth-service
public record AdminProfileResponse(
    String userId,
    String username,
    String displayName,
    String country,
    String visibility,
    boolean onboardingCompleted,
    int followersCount,
    int followingCount,
    int favoritesCount
) {}
