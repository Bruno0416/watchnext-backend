package com.watchnext.auth_service.dto.social;

import com.watchnext.auth_service.enums.AuthProvider;

public record SocialUserInfo(
    AuthProvider provider,
    String providerUserId,
    String email,
    boolean emailVerified,
    String name,
    String pictureUrl
) {}
