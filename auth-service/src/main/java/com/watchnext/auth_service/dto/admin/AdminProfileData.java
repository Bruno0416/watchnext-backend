package com.watchnext.auth_service.dto.admin;

// lo que devuelve user-service del perfil de cada cuenta
public record AdminProfileData(
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
