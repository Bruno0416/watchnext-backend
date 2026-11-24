package com.watchnext.user_service.service.social;

import com.watchnext.common.exceptions.WatchNextException;
import com.watchnext.common.security.CurrentUser;
import com.watchnext.user_service.dto.ProfileSummaryResponse;
import com.watchnext.user_service.dto.RelationshipResponse;
import com.watchnext.user_service.entity.Follow;
import com.watchnext.user_service.entity.Profile;
import com.watchnext.user_service.enums.FollowStatus;
import com.watchnext.user_service.enums.ProfileVisibility;
import com.watchnext.user_service.exception.ProfileNotFound;
import com.watchnext.user_service.mapper.ProfileMapper;
import com.watchnext.user_service.repository.FollowRepository;
import com.watchnext.user_service.repository.ProfileRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialQueryServiceImpl implements SocialQueryService {

    private final ProfileRepository profileRepo;
    private final FollowRepository followRepo;
    private final ProfileMapper mapper;

    // ---------- seguidores y seguidos de cualquier perfil ----------
    @Override
    @Transactional(readOnly = true)
    public Page<ProfileSummaryResponse> getFollowers(String username, Pageable pageable) {
        // 1. buscar el perfil y validar que se pueda ver
        Profile target = visibleProfile(username);

        // 2. seguidores aceptados -> summary del follower
        return followRepo
            .findByFolloweeAndStatus(target, FollowStatus.ACCEPTED, pageable)
            .map(f -> mapper.toSummary(f.getFollower()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfileSummaryResponse> getFollowing(String username, Pageable pageable) {
        Profile target = visibleProfile(username);
        return followRepo
            .findByFollowerAndStatus(target, FollowStatus.ACCEPTED, pageable)
            .map(f -> mapper.toSummary(f.getFollowee()));
    }

    // ---------- relacion con otro perfil ----------
    @Override
    @Transactional(readOnly = true)
    public RelationshipResponse getRelationship(String username) {
        // 1. perfil propio y perfil objetivo
        Profile me = profileRepo.findByUserId(CurrentUser.id()).orElseThrow(ProfileNotFound::new);
        Profile target = profileRepo.findByUsername(username).orElseThrow(ProfileNotFound::new);

        // 2. lo que yo hice hacia el y lo que el hizo hacia mi
        Optional<Follow> mine = followRepo.findByFollowerAndFollowee(me, target);
        boolean followsYou = followRepo
            .findByFollowerAndFollowee(target, me)
            .map(f -> f.getStatus() == FollowStatus.ACCEPTED)
            .orElse(false);

        return new RelationshipResponse(
            mine.map(f -> f.getStatus() == FollowStatus.ACCEPTED).orElse(false),
            mine.map(f -> f.getStatus() == FollowStatus.PENDING).orElse(false),
            followsYou
        );
    }

    // ---------- helpers ----------
    private Profile visibleProfile(String username) {
        // 1. el perfil tiene que existir
        Profile target = profileRepo.findByUsername(username).orElseThrow(ProfileNotFound::new);
        if (target.getVisibility() != ProfileVisibility.PRIVATE) return target;

        // 2. privado: solo el dueño o un seguidor aceptado ven sus listas
        Profile viewer = profileRepo.findByUserId(CurrentUser.id()).orElse(null);
        if (viewer != null && viewer.getId().equals(target.getId())) return target;
        boolean accepted = viewer != null && followRepo
            .findByFollowerAndFollowee(viewer, target)
            .map(f -> f.getStatus() == FollowStatus.ACCEPTED)
            .orElse(false);
        if (!accepted) throw new WatchNextException(HttpStatus.FORBIDDEN, "Este perfil es privado");
        return target;
    }
}
