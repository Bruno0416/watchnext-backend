package com.watchnext.user_service.service.admin;

import com.watchnext.user_service.dto.AdminProfileResponse;
import com.watchnext.user_service.dto.InternalIdentityRequest;
import com.watchnext.user_service.entity.Follow;
import com.watchnext.user_service.entity.Profile;
import com.watchnext.user_service.enums.FollowStatus;
import com.watchnext.user_service.exception.ProfileNotFound;
import com.watchnext.user_service.exception.UsernameAlreadyTaken;
import com.watchnext.user_service.exception.UsernameReserved;
import com.watchnext.user_service.repository.FollowRepository;
import com.watchnext.user_service.repository.ProfileRepository;
import com.watchnext.user_service.service.UserServiceImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminProfileServiceImpl implements AdminProfileService {

    private final ProfileRepository profileRepo;
    private final FollowRepository followRepo;

    // ---------- consultas ----------
    @Override
    @Transactional(readOnly = true)
    public List<AdminProfileResponse> getProfiles(List<String> authUserIds) {
        // 1. sin ids no hay nada que buscar
        if (authUserIds == null || authUserIds.isEmpty()) return List.of();

        // 2. traer los perfiles de esas cuentas y mapearlos
        return profileRepo.findAllByUserIdIn(authUserIds).stream().map(this::toResponse).toList();
    }

    // ---------- identidad ----------
    @Override
    @Transactional
    public AdminProfileResponse updateIdentity(String authUserId, InternalIdentityRequest request) {
        // 1. el perfil lo crea auth-service al registrar
        Profile profile = profileRepo.findByUserId(authUserId).orElseThrow(ProfileNotFound::new);

        // 2. mismas reglas que el onboarding: no reservado y no tomado por otro
        String username = request.username().trim();
        if (UserServiceImpl.RESERVED_USERNAMES.contains(username.toLowerCase())) {
            throw new UsernameReserved(username);
        }
        profileRepo.findByUsername(username)
            .filter(other -> !other.getUserId().equals(authUserId))
            .ifPresent(other -> {
                throw new UsernameAlreadyTaken(username);
            });

        // 3. cambiar solo username y nombre visible
        String displayName = request.displayName() == null ? "" : request.displayName().trim();
        profile.setUsername(username);
        profile.setDisplayName(displayName.isEmpty() ? null : displayName);
        return toResponse(profileRepo.save(profile));
    }

    // ---------- baja ----------
    @Override
    @Transactional
    public void deleteProfile(String authUserId) {
        // 1. si no tiene perfil no hay nada que borrar
        Profile profile = profileRepo.findByUserId(authUserId).orElse(null);
        if (profile == null) return;

        // 2. borrar sus relaciones y descontar los contadores del otro lado
        List<Follow> follows = followRepo.findByFollowerOrFollowee(profile, profile);
        for (Follow follow : follows) {
            if (follow.getStatus() != FollowStatus.ACCEPTED) continue;
            if (follow.getFollower().getId().equals(profile.getId())) {
                Profile followee = follow.getFollowee();
                followee.setFollowersCount(Math.max(0, followee.getFollowersCount() - 1));
                profileRepo.save(followee);
            } else {
                Profile follower = follow.getFollower();
                follower.setFollowingCount(Math.max(0, follower.getFollowingCount() - 1));
                profileRepo.save(follower);
            }
        }
        followRepo.deleteAll(follows);

        // 3. borrar el perfil (los favoritos se van con el)
        profileRepo.delete(profile);
    }

    // ---------- helpers ----------
    private AdminProfileResponse toResponse(Profile profile) {
        return new AdminProfileResponse(
            profile.getUserId(),
            profile.getUsername(),
            profile.getDisplayName(),
            profile.getCountry(),
            profile.getVisibility() == null ? null : profile.getVisibility().name(),
            profile.isOnboardingCompleted(),
            profile.getFollowersCount(),
            profile.getFollowingCount(),
            profile.getFavorites() == null ? 0 : profile.getFavorites().size()
        );
    }
}
