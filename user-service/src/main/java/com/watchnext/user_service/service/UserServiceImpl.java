package com.watchnext.user_service.service;

import com.watchnext.common.context.UserContext;
import com.watchnext.common.dto.ContentRefRequest;
import com.watchnext.common.dto.internal.ContentBasicDetail;
import com.watchnext.common.security.CurrentUser;
import com.watchnext.common.util.CountryCodes;
import com.watchnext.user_service.client.ContentServiceClient;
import com.watchnext.user_service.dto.FavoriteItemRequest;
import com.watchnext.user_service.dto.FavoritesRequest;
import com.watchnext.user_service.dto.InternalFollowingResponse;
import com.watchnext.user_service.dto.OnboardingRequest;
import com.watchnext.user_service.dto.ProfileResponse;
import com.watchnext.user_service.dto.ProfileSummaryResponse;
import com.watchnext.user_service.dto.UpdateProfileRequest;
import com.watchnext.user_service.dto.UsernameAvailabilityResponse;
import com.watchnext.user_service.entity.Follow;
import com.watchnext.common.model.ContentRef;
import com.watchnext.user_service.entity.Profile;
import com.watchnext.user_service.enums.FollowStatus;
import com.watchnext.user_service.enums.ProfileVisibility;
import com.watchnext.user_service.exception.AlreadyFollowing;
import com.watchnext.user_service.exception.CountryNotFound;
import com.watchnext.user_service.exception.FollowRequestNotFound;
import com.watchnext.user_service.exception.InvalidCountry;
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

    // ---------- consultas de perfil ----------
    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(String language) {
        // 1. obtener perfil del usuario
        Profile profile = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);

        // 2. mapear favoritos a referencias para el bulkfetch
        List<ContentRefRequest> requests = contentRefMapper.toRequestList(
            profile.getFavorites()
        );

        // 3. hidratar metadata desde content-service y mapear a la respuesta
        List<ContentBasicDetail> contents = contentClient.fetchBulkContent(
            requests,
            language
        );

        return mapper.toResponse(profile, contents);
    }

    // ---------- operaciones de perfil ----------
    @Override
    @Transactional
    public void completeOnboarding(
        OnboardingRequest request,
        MultipartFile avatar
    ) {
        // 1. validar usuario
        String userId = CurrentUser.id();

        // 2. validar @username: no reservado y no tomado por otro perfil
        String username = request.username();
        if (RESERVED_USERNAMES.contains(username.toLowerCase())) {
            throw new UsernameReserved(username);
        }
        if (profileRepo.existsByUsername(username)) {
            throw new UsernameAlreadyTaken(username);
        }

        // 3. validar y normalizar el codigo de pais obligatorio en onboarding
        String country = normalizeAndValidateCountry(request.country());

        // 4. crear nuevo perfil con datos base del onboarding
        Profile profile = Profile.builder()
            .userId(userId)
            .username(username)
            .visibility(request.visibility())
            .displayName(request.displayName())
            .bio(request.bio())
            .country(country)
            .onboardingCompleted(true)
            .build();

        // 5. agregar el top n de favoritos si vienen en la request
        if (request.favorites() != null && !request.favorites().isEmpty()) {
            // 5.1. crear una copia de los favoritos entrantes y ordenarla basándose en el parámetro position
            List<FavoriteItemRequest> sortedRequests = new java.util.ArrayList<>(request.favorites());
            sortedRequests.sort((a, b) -> {
                int posA = a.position() != null ? a.position() : Integer.MAX_VALUE;
                int posB = b.position() != null ? b.position() : Integer.MAX_VALUE;
                return Integer.compare(posA, posB);
            });
            // 5.2. mapear a entidades y agregarlas a la lista del perfil
            profile.getFavorites().addAll(contentRefMapper.toModelListFromFavorites(sortedRequests));
        }

        // 6. guardar para obtener el id uuid necesario para el avatar
        profile = profileRepo.save(profile);

        // 7. subir avatar si viene en la request
        if (avatar != null && !avatar.isEmpty()) {
            String url = avatarService.upload(avatar, profile.getId());
            profile.setAvatarUrl(url);
            profileRepo.save(profile);
        }
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(
        UpdateProfileRequest request,
        String language
    ) {
        // 1. recuperar perfil actual
        Profile profile = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);
        // 2. actualizar campos basico
        if (request.displayName() != null) profile.setDisplayName(
            request.displayName()
        );
        if (request.bio() != null) profile.setBio(request.bio());
        if (request.visibility() != null) profile.setVisibility(
            request.visibility()
        );
        if (request.country() != null) profile.setCountry(
            normalizeAndValidateCountry(request.country())
        );
        // 3. persistir cambios
        if (request.favorites() != null) {
            reconcileFavorites(profile, request.favorites());
        }
        profileRepo.save(profile);
        // 4. hidratar favoritos y devolver el perfil actualizado
        List<ContentRefRequest> requests = contentRefMapper.toRequestList(
            profile.getFavorites()
        );
        List<ContentBasicDetail> contents = contentClient.fetchBulkContent(
            requests,
            language
        );
        return mapper.toResponse(profile, contents);
    }

    // ---------- avatar ----------
    @Override
    @Transactional
    public void uploadAvatar(MultipartFile file) {
        // 1. obtener perfil del usuario autenticado
        Profile profile = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);
        // 2. subir archivo al object storage y guardar la url
        String url = avatarService.upload(file, profile.getId());
        profile.setAvatarUrl(url);
        profileRepo.save(profile);
    }

    @Override
    @Transactional
    public void deleteAvatar() {
        // 1. obtener perfil del usuario autenticado
        Profile profile = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);
        // 2. borrar archivo del object storage y limpiar la url
        avatarService.delete(profile.getId());
        profile.setAvatarUrl(null);
        profileRepo.save(profile);
    }

    // ---------- favoritos ----------
    @Override
    @Transactional
    public void replaceFavorites(FavoritesRequest request) {
        // 1. obtener perfil del usuario autenticado
        Profile profile = profileRepo
            .findByUserId(CurrentUser.id())
            .orElseThrow(ProfileNotFound::new);
        // 2. reconciliar la lista entrante contra la persistida
        reconcileFavorites(profile, request.items());
        profileRepo.save(profile);
    }

    // --- helper privado ---
    private String normalizeAndValidateCountry(String rawCountry) {
        // 1. normalizar a mayusculas y validar contra el set iso 3166-1 alpha-2
        String normalized = CountryCodes.normalize(rawCountry);
        if (!CountryCodes.isValid(normalized)) {
            throw new InvalidCountry(rawCountry);
        }
        return normalized;
    }

    private void reconcileFavorites(
        Profile profile,
        List<FavoriteItemRequest> items
    ) {
        // 1. validar el tope maximo configurable
        if (items.size() > favoritesMaxSize) {
            throw new IllegalArgumentException(
                "Máximo " + favoritesMaxSize + " favoritos permitidos"
            );
        }

        // 2. validar que no haya tmdbid duplicados
        long distinctIds = items
            .stream()
            .map(FavoriteItemRequest::tmdbId)
            .distinct()
            .count();
        if (distinctIds != items.size()) {
            throw new IllegalArgumentException("No se permiten tmdbId duplicados");
        }

        // 3. ordenar por position explicito
        List<FavoriteItemRequest> sortedRequests = new java.util.ArrayList<>(items);
        sortedRequests.sort((a, b) -> {
            int posA = a.position() != null ? a.position() : Integer.MAX_VALUE;
            int posB = b.position() != null ? b.position() : Integer.MAX_VALUE;
            return Integer.compare(posA, posB);
        });

        // 4. mapear a las nuevas entidades
        List<ContentRef> newFavorites = contentRefMapper.toModelListFromFavorites(
            sortedRequests
        );

        // 5. vaciamos la lista en la memoria de hibernate
        profile.getFavorites().clear();

        // 6. obligamos a hibernate a ejecutar el delete en la bd
        profileRepo.saveAndFlush(profile);

        // 7. añadimos los nuevos favoritos limpios
        profile.getFavorites().addAll(newFavorites);

        List<ContentRef> currentFavorites = profile.getFavorites();
        currentFavorites.retainAll(newFavorites);

        for (int i = 0; i < newFavorites.size(); i++) {
            ContentRef expected = newFavorites.get(i);
            if (i < currentFavorites.size()) {
                if (!currentFavorites.get(i).equals(expected)) {
                    int idx = currentFavorites.indexOf(expected);
                    if (idx > i) {
                        ContentRef removed = currentFavorites.remove(idx);
                        currentFavorites.add(i, removed);
                    } else {
                        currentFavorites.add(i, expected);
                    }
                }
            } else {
                currentFavorites.add(expected);
            }
        }

        // 8. limpiar elementos excedentes
        while (currentFavorites.size() > newFavorites.size()) {
            currentFavorites.remove(currentFavorites.size() - 1);
        }
    }

    // ---------- disponibilidad y busqueda ----------
    @Override
    @Transactional(readOnly = true)
    public UsernameAvailabilityResponse checkUsernameAvailable(
        String username
    ) {
        // 1. disponible solo si no es reservado y no existe en la bd
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

    @Override
    @Transactional(readOnly = true)
    public Page<ProfileSummaryResponse> searchPublicProfiles(
        String q,
        Pageable pageable
    ) {
        // 1. buscar solo perfiles publicos por username o displayName
        return profileRepo.searchPublicProfiles(
            ProfileVisibility.PUBLIC,
            q,
            pageable
        );
    }

	@Override
	public String findCountryByUserId(String userId) {
	    // 1. obtener pais
	    return profileRepo.findCountryByUserId(userId).orElseThrow(CountryNotFound::new);
	}

    @Override
    public UserContext getUserContext(String userId) {
        return profileRepo.findByUserId(userId)
            .map(p -> new UserContext(
                p.getCountry() != null ? p.getCountry() : "US",
                p.getRegion() != null ? p.getRegion() : "DEFAULT"))
            .orElseThrow(CountryNotFound::new);
    }
}
