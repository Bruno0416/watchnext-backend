package com.watchnext.user_service.service;

import com.watchnext.common.dto.ContentRefRequest;
import com.watchnext.common.dto.internal.ContentBasicDetail;
import com.watchnext.common.security.CurrentUser;
import com.watchnext.user_service.client.ContentServiceClient;
import com.watchnext.user_service.dto.FavoritesRequest;
import com.watchnext.user_service.dto.InternalFollowingResponse;
import com.watchnext.user_service.dto.OnboardingRequest;
import com.watchnext.user_service.dto.ProfileResponse;
import com.watchnext.user_service.dto.ProfileSummaryResponse;
import com.watchnext.user_service.dto.UpdateProfileRequest;
import com.watchnext.user_service.dto.UsernameAvailabilityResponse;
import com.watchnext.user_service.entity.Follow;
import com.watchnext.user_service.entity.Profile;
import com.watchnext.user_service.enums.FollowStatus;
import com.watchnext.user_service.enums.ProfileVisibility;
import com.watchnext.user_service.exception.AlreadyFollowing;
import com.watchnext.user_service.exception.FollowRequestNotFound;
import com.watchnext.user_service.exception.ProfileNotFound;
import com.watchnext.user_service.exception.UsernameAlreadyTaken;
import com.watchnext.user_service.exception.UsernameReserved;
import com.watchnext.user_service.mapper.ContentRefMapper;
import com.watchnext.user_service.mapper.ProfileMapper;
import com.watchnext.user_service.repository.FollowRepository;
import com.watchnext.user_service.repository.ProfileRepository;
import com.watchnext.user_service.service.avatar.AvatarService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // --- Constantes ---
    private static final Set<String> RESERVED_USERNAMES = Set.of(
        "admin",
        "api",
        "me",
        "internal",
        "support",
        "help",
        "watchnext",
        "official",
        "system",
        "null",
        "undefined"
    );

    // --- Repositorios ---
    private final ProfileRepository profileRepo;
    private final FollowRepository followRepo;

    // --- Mappers ---
    private final ProfileMapper mapper;
    private final ContentRefMapper contentRefMapper;

    // --- Clientes ---
    private final ContentServiceClient contentClient;

    // --- Servicios ---
    private final AvatarService avatarService;

    // --- Config ---
    @Value("${app.profile.favorites.max-size}")
    private int favoritesMaxSize;

    // --- Profile propio ---

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(String language) {
        // 1. Obtener perfil del usuario.
        Profile profile = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);

        // 2. Mapear favoritos a referencias para el bulkfetch.
        List<ContentRefRequest> requests = contentRefMapper.toRequestList(
            profile.getFavorites()
        );

        // 3. Hidratar metadata desde content-service y mapear a la respuesta.
        List<ContentBasicDetail> contents = contentClient.fetchBulkContent(
            requests,
            language
        );

        return mapper.toResponse(profile, contents);
    }

    @Override
    @Transactional
    public void completeOnboarding(
        OnboardingRequest request,
        MultipartFile avatar
    ) {
        // 1. Validar usuario.
        String userId = CurrentUser.id();

        // 2. Validar @username: no reservado y no tomado por otro perfil.
        String username = request.username();
        if (RESERVED_USERNAMES.contains(username.toLowerCase())) {
            throw new UsernameReserved(username);
        }
        if (profileRepo.existsByUsername(username)) {
            throw new UsernameAlreadyTaken(username);
        }

        // 3. Crear nuevo perfil con datos base del onboarding.
        Profile profile = Profile.builder()
            .userId(userId)
            .username(username)
            .visibility(request.visibility())
            .displayName(request.displayName())
            .bio(request.bio())
            .onboardingCompleted(true)
            .build();

        // 4. Agregar el top N de favoritos si vienen en la request.
        if (request.favorites() != null && !request.favorites().isEmpty()) {
            profile
                .getFavorites()
                .addAll(contentRefMapper.toModelList(request.favorites()));
        }

        // 5. Guardar para obtener el ID (UUID) necesario para el avatar.
        profile = profileRepo.save(profile);

        // 6. Subir avatar si viene en la request.
        if (avatar != null && !avatar.isEmpty()) {
            String url = avatarService.upload(avatar, profile.getId());
            profile.setAvatarUrl(url);
            profileRepo.save(profile);
        }
    }

    @Override
    @Transactional
    public void updateProfile(UpdateProfileRequest request) {
        // 1. Obtener perfil del usuario autenticado.
        Profile profile = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);

        // 2. Actualizar solo los campos presentes.
        if (request.displayName() != null) profile.setDisplayName(
            request.displayName()
        );
        if (request.bio() != null) profile.setBio(request.bio());
        if (request.visibility() != null) profile.setVisibility(
            request.visibility()
        );

        // 3. Persistir.
        profileRepo.save(profile);
    }

    @Override
    @Transactional
    public void uploadAvatar(MultipartFile file) {
        // 1. Obtener perfil del usuario autenticado.
        Profile profile = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);
        // 2. Subir archivo al object storage y guardar la URL.
        String url = avatarService.upload(file, profile.getId());
        profile.setAvatarUrl(url);
        profileRepo.save(profile);
    }

    @Override
    @Transactional
    public void deleteAvatar() {
        // 1. Obtener perfil del usuario autenticado.
        Profile profile = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);
        // 2. Borrar archivo del object storage y limpiar la URL.
        avatarService.delete(profile.getId());
        profile.setAvatarUrl(null);
        profileRepo.save(profile);
    }

    @Override
    @Transactional
    public void replaceFavorites(FavoritesRequest request) {
        // 1. Validar el tope configurable de favoritos.
        if (request.items().size() > favoritesMaxSize) {
            throw new IllegalArgumentException(
                "Máximo " + favoritesMaxSize + " favoritos permitidos"
            );
        }
        // 2. Obtener perfil del usuario autenticado.
        Profile profile = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);
        // 3. Reemplazar el top N completo (delete + insert dentro de la transaccion)
        profile.getFavorites().clear();
        profile
            .getFavorites()
            .addAll(contentRefMapper.toModelList(request.items()));
        profileRepo.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public UsernameAvailabilityResponse checkUsernameAvailable(
        String username
    ) {
        // 1. Disponible solo si no es reservado y no existe en la BD.
        boolean available =
            !RESERVED_USERNAMES.contains(username.toLowerCase()) &&
            !profileRepo.existsByUsername(username);
        return new UsernameAvailabilityResponse(available);
    }

    // --- Perfiles publicos ---

    @Override
    @Transactional(readOnly = true)
    public Object getProfile(String username, String language) {
        // 1. Buscar el perfil objetivo por @username.
        Profile target = profileRepo
            .findByUsername(username)
            .orElseThrow(ProfileNotFound::new);

        // 2. Si el viewer es el dueño -> respuesta completa.
        if (target.getUserId().equals(CurrentUser.id())) {
            List<ContentRefRequest> requests = contentRefMapper.toRequestList(
                target.getFavorites()
            );
            List<ContentBasicDetail> contents = contentClient.fetchBulkContent(
                requests,
                language
            );
            return mapper.toResponse(target, contents);
        }

        // 3. Determinar si el viewer sigue (ACCEPTED) al perfil objetivo.
        Profile viewer = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);

        boolean isFollowing = followRepo
            .findByFollowerAndFollowee(viewer, target)
            .map(f -> f.getStatus() == FollowStatus.ACCEPTED)
            .orElse(false);

        // 4. Perfil privado y no lo sigue -> respuesta limitada (puede mandar request)
        if (
            target.getVisibility() == ProfileVisibility.PRIVATE && !isFollowing
        ) {
            return mapper.toPublicResponse(target);
        }

        // 5. Publico, o privado pero seguido -> respuesta completa.
        List<ContentRefRequest> requests = contentRefMapper.toRequestList(
            target.getFavorites()
        );
        List<ContentBasicDetail> contents = contentClient.fetchBulkContent(
            requests,
            language
        );
        return mapper.toResponse(target, contents);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfileSummaryResponse> searchProfiles(
        String q,
        Pageable pageable
    ) {
        // 1. Buscar por username o display name y mapear a summary.
        return profileRepo
            .findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
                q,
                q,
                pageable
            )
            .map(mapper::toSummary);
    }

    // --- Follow ---

    @Override
    @Transactional
    public void follow(String username) {
        // 1. Obtener perfil propio (follower) y el objetivo (followee)
        Profile follower = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);
        Profile followee = profileRepo
            .findByUsername(username)
            .orElseThrow(ProfileNotFound::new);

        // 2. No permitir auto-follow.
        if (follower.getId().equals(followee.getId())) {
            throw new AlreadyFollowing();
        }

        // 3. No permitir follow duplicado.
        if (followRepo.existsByFollowerAndFollowee(follower, followee)) {
            throw new AlreadyFollowing();
        }

        // 4. PUBLIC -> ACCEPTED directo | PRIVATE -> PENDING hasta aprobacion
        FollowStatus status =
            followee.getVisibility() == ProfileVisibility.PUBLIC
                ? FollowStatus.ACCEPTED
                : FollowStatus.PENDING;

        followRepo.save(
            Follow.builder()
                .follower(follower)
                .followee(followee)
                .status(status)
                .build()
        );

        // 5. Si quedo ACCEPTED, actualizar counts en la misma transaccion.
        if (status == FollowStatus.ACCEPTED) {
            follower.setFollowingCount(follower.getFollowingCount() + 1);
            followee.setFollowersCount(followee.getFollowersCount() + 1);
            profileRepo.save(follower);
            profileRepo.save(followee);
        }
    }

    @Override
    @Transactional
    public void unfollow(String username) {
        // 1. Obtener perfil propio (follower) y el objetivo (followee)
        Profile follower = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);
        Profile followee = profileRepo
            .findByUsername(username)
            .orElseThrow(ProfileNotFound::new);

        // 2. Si existe la relacion: decrementar counts (si era ACCEPTED) y borrarla.
        followRepo
            .findByFollowerAndFollowee(follower, followee)
            .ifPresent(follow -> {
                if (follow.getStatus() == FollowStatus.ACCEPTED) {
                    follower.setFollowingCount(
                        Math.max(0, follower.getFollowingCount() - 1)
                    );
                    followee.setFollowersCount(
                        Math.max(0, followee.getFollowersCount() - 1)
                    );
                    profileRepo.save(follower);
                    profileRepo.save(followee);
                }
                followRepo.delete(follow);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfileSummaryResponse> getMyFollowers(Pageable pageable) {
        // 1. Obtener perfil propio.
        Profile profile = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);
        // 2. Seguidores ACCEPTED -> summary del follower.
        return followRepo
            .findByFolloweeAndStatus(profile, FollowStatus.ACCEPTED, pageable)
            .map(f -> mapper.toSummary(f.getFollower()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfileSummaryResponse> getMyFollowing(Pageable pageable) {
        // 1. Obtener perfil propio.
        Profile profile = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);
        // 2. Seguidos ACCEPTED -> summary del followee.
        return followRepo
            .findByFollowerAndStatus(profile, FollowStatus.ACCEPTED, pageable)
            .map(f -> mapper.toSummary(f.getFollowee()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfileSummaryResponse> getFollowRequests(Pageable pageable) {
        // 1. Obtener perfil propio.
        Profile profile = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);
        // 2. Requests entrantes PENDING -> summary del solicitante.
        return followRepo
            .findByFolloweeAndStatus(profile, FollowStatus.PENDING, pageable)
            .map(f -> mapper.toSummary(f.getFollower()));
    }

    @Override
    @Transactional
    public void acceptFollowRequest(String username) {
        // 1. Obtener perfil propio y el del solicitante.
        Profile me = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);
        Profile requester = profileRepo
            .findByUsername(username)
            .orElseThrow(ProfileNotFound::new);

        // 2. Buscar el request PENDING dirigido a 'mi'.
        Follow follow = followRepo
            .findByFollowerAndFollowee(requester, me)
            .filter(f -> f.getStatus() == FollowStatus.PENDING)
            .orElseThrow(FollowRequestNotFound::new);

        // 3. Aceptar y actualizar counts en la misma transaccion.
        follow.setStatus(FollowStatus.ACCEPTED);
        me.setFollowersCount(me.getFollowersCount() + 1);
        requester.setFollowingCount(requester.getFollowingCount() + 1);

        followRepo.save(follow);
        profileRepo.save(me);
        profileRepo.save(requester);
    }

    @Override
    @Transactional
    public void rejectFollowRequest(String username) {
        // 1. Obtener perfil propio y el del solicitante.
        Profile me = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);
        Profile requester = profileRepo
            .findByUsername(username)
            .orElseThrow(ProfileNotFound::new);

        // 2. Buscar el request PENDING dirigido a 'mi'.
        Follow follow = followRepo
            .findByFollowerAndFollowee(requester, me)
            .filter(f -> f.getStatus() == FollowStatus.PENDING)
            .orElseThrow(FollowRequestNotFound::new);

        // 3. Borrar el request (sin tocar counts, nunca fue ACCEPTED)
        followRepo.delete(follow);
    }

    // --- Internos ---

    @Override
    @Transactional(readOnly = true)
    public InternalFollowingResponse getFollowingUserIds(String userId) {
        Profile profile = profileRepo
            .findByUserId(userId)
            .orElseThrow(ProfileNotFound::new);

        // 2. Devolver los userId seguidos (ACCEPTED) para el feed de review-service.
        List<String> ids = followRepo
            .findByFollowerAndStatus(profile, FollowStatus.ACCEPTED)
            .stream()
            .map(f -> f.getFollowee().getUserId())
            .collect(Collectors.toList());

        return new InternalFollowingResponse(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileSummaryResponse> bulkGetProfiles(List<String> userIds) {
        // 2. Hidratar perfiles (username, avatar) para pintar autores de reviews.
        return mapper.toSummaryList(profileRepo.findAllByUserIdIn(userIds));
    }
}
