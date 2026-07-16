package com.watchnext.user_service.service;

import com.watchnext.common.context.UserContext;
import com.watchnext.user_service.dto.FavoritesRequest;
import com.watchnext.user_service.dto.InternalFollowingResponse;
import com.watchnext.user_service.dto.OnboardingRequest;
import com.watchnext.user_service.dto.ProfileResponse;
import com.watchnext.user_service.dto.ProfileSummaryResponse;
import com.watchnext.user_service.dto.UpdateProfileRequest;
import com.watchnext.user_service.dto.UsernameAvailabilityResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    // ──────────────────── Perfil Propio ────────────────────
    ProfileResponse getMyProfile(String language);

    void completeOnboarding(OnboardingRequest request, MultipartFile avatar);

    ProfileResponse updateProfile(UpdateProfileRequest request, String language);

    // ──────────────────── Gestion de Avatar ────────────────────
    void uploadAvatar(MultipartFile file);

    void deleteAvatar();

    // ──────────────────── Favoritos ────────────────────
    void replaceFavorites(FavoritesRequest request);

    // ──────────────────── Consulta y Busqueda de Perfiles ────────────────────
    UsernameAvailabilityResponse checkUsernameAvailable(String username);

    Object getProfile(String username, String language);

    Page<ProfileSummaryResponse> searchProfiles(String q, Pageable pageable);

    // ──────────────────── Seguidores y Seguidos ────────────────────
    void follow(String username);

    void unfollow(String username);

    Page<ProfileSummaryResponse> getMyFollowers(Pageable pageable);

    Page<ProfileSummaryResponse> getMyFollowing(Pageable pageable);

    // ──────────────────── Solicitudes de Seguimiento ────────────────────
    Page<ProfileSummaryResponse> getFollowRequests(Pageable pageable);

    void acceptFollowRequest(String username);

    void rejectFollowRequest(String username);

    // ──────────────────── metodos internos ────────────────────
    InternalFollowingResponse getFollowingUserIds(String authUserId);

    List<ProfileSummaryResponse> bulkGetProfiles(List<String> authUserIds);

    Page<ProfileSummaryResponse> searchPublicProfiles(String q, Pageable pageable);

    String findCountryByUserId(String userId);

    UserContext getUserContext(String userId);
}
