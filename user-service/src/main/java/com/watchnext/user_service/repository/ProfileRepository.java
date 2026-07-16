package com.watchnext.user_service.repository;

import com.watchnext.user_service.dto.ProfileSummaryResponse;
import com.watchnext.user_service.entity.Profile;
import com.watchnext.user_service.enums.ProfileVisibility;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByUserId(String userId);

    Optional<Profile> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<Profile> findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
        String username,
        String displayName,
        Pageable pageable
    );

    List<Profile> findAllByUserIdIn(Collection<String> userIds);

    @Query("""
        SELECT new com.watchnext.user_service.dto.ProfileSummaryResponse(
            p.userId, p.username, p.displayName, p.avatarUrl
        )
        FROM Profile p
        WHERE p.visibility = :visibility
          AND (LOWER(p.username) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.displayName) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY CASE WHEN LOWER(p.username) = LOWER(:q) THEN 0 ELSE 1 END, p.followersCount DESC
        """)
    Page<ProfileSummaryResponse> searchPublicProfiles(
        @Param("visibility") ProfileVisibility visibility,
        @Param("q") String q,
        Pageable pageable
    );

    @Query("SELECT p.country FROM Profile p WHERE p.userId = :userId")
    Optional<String> findCountryByUserId(@Param("userId") String userId);
}
