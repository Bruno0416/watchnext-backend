package com.watchnext.user_service.entity;

import com.watchnext.common.model.ContentRef;
import com.watchnext.user_service.enums.ProfileVisibility;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "profiles",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_profiles_user_id",
            columnNames = "user_id"
        ),
        @UniqueConstraint(
            name = "uk_profiles_username",
            columnNames = "username"
        ),
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(exclude = "favorites")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(name = "display_name", length = 50)
    private String displayName;

    @Column(length = 500)
    private String bio;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "country", length = 2)
    private String country;

    @Column(name = "region", length = 16)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private ProfileVisibility visibility = ProfileVisibility.PRIVATE;

    @Column(name = "onboarding_completed", nullable = false)
    @Builder.Default
    private boolean onboardingCompleted = false;

    @Column(name = "followers_count", nullable = false)
    @Builder.Default
    private int followersCount = 0;

    @Column(name = "following_count", nullable = false)
    @Builder.Default
    private int followingCount = 0;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "favorite_content",
        joinColumns = @JoinColumn(name = "profile_id"),
        uniqueConstraints = @UniqueConstraint(
            name = "uk_favorite_profile_content",
            columnNames = { "profile_id", "tmdb_id", "media_type" }
        )
    )
    @OrderColumn(name = "position")
    @Builder.Default
    private List<ContentRef> favorites = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
