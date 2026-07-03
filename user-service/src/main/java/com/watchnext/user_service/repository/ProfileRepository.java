package com.watchnext.user_service.repository;

import com.watchnext.user_service.entity.Profile;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
