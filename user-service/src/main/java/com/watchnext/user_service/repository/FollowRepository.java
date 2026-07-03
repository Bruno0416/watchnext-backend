package com.watchnext.user_service.repository;

import com.watchnext.user_service.entity.Follow;
import com.watchnext.user_service.entity.Profile;
import com.watchnext.user_service.enums.FollowStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    Optional<Follow> findByFollowerAndFollowee(
        Profile follower,
        Profile followee
    );

    boolean existsByFollowerAndFollowee(Profile follower, Profile followee);

    Page<Follow> findByFolloweeAndStatus(
        Profile followee,
        FollowStatus status,
        Pageable pageable
    );

    Page<Follow> findByFollowerAndStatus(
        Profile follower,
        FollowStatus status,
        Pageable pageable
    );

    List<Follow> findByFolloweeAndStatus(Profile followee, FollowStatus status);

    List<Follow> findByFollowerAndStatus(Profile follower, FollowStatus status);
}
