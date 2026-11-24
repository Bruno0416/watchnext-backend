package com.watchnext.auth_service.dto.admin;

import com.watchnext.auth_service.entity.Users;
import java.time.LocalDateTime;

// cuenta de auth-service junto con su perfil de user-service (nunca la clave)
public record AdminUserResponse(
    String userId,
    String email,
    String role,
    boolean emailVerified,
    String username,
    String displayName,
    String country,
    String visibility,
    boolean onboardingCompleted,
    int followersCount,
    int followingCount,
    int favoritesCount,
    LocalDateTime createdAt
) {
    public static AdminUserResponse of(Users user, AdminProfileData profile) {
        return new AdminUserResponse(
            user.getId().toString(),
            user.getEmail(),
            user.getRole().name(),
            user.isEmailVerified(),
            profile == null ? null : profile.username(),
            profile == null ? null : profile.displayName(),
            profile == null ? null : profile.country(),
            profile == null ? null : profile.visibility(),
            profile != null && profile.onboardingCompleted(),
            profile == null ? 0 : profile.followersCount(),
            profile == null ? 0 : profile.followingCount(),
            profile == null ? 0 : profile.favoritesCount(),
            user.getCreatedAt()
        );
    }
}
