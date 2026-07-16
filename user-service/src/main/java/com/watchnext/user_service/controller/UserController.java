package com.watchnext.user_service.controller;

import com.watchnext.user_service.dto.FavoritesRequest;
import com.watchnext.user_service.dto.OnboardingRequest;
import com.watchnext.user_service.dto.ProfileResponse;
import com.watchnext.user_service.dto.ProfileSummaryResponse;
import com.watchnext.user_service.dto.UpdateProfileRequest;
import com.watchnext.user_service.dto.UsernameAvailabilityResponse;
import com.watchnext.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
public class UserController {

    private final UserService service;

    // ---------- perfil ----------
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return ResponseEntity.ok(service.getMyProfile(language));
    }

    // ---------- onboarding ----------
    @PutMapping(
        value = "/me/onboarding",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> completeOnboarding(
        @RequestPart("data") @Valid OnboardingRequest data,
        @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        service.completeOnboarding(data, avatar);
        return ResponseEntity.noContent().build();
    }

    // ---------- perfil ----------
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
        @Valid @RequestBody UpdateProfileRequest request,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return ResponseEntity.ok(service.updateProfile(request, language));
    }

    // ---------- avatar ----------
    @PostMapping(
        value = "/me/avatar",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> uploadAvatar(
        @RequestPart("avatar") MultipartFile avatar
    ) {
        service.uploadAvatar(avatar);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<Void> deleteAvatar() {
        service.deleteAvatar();
        return ResponseEntity.noContent().build();
    }

    // ---------- favoritos ----------
    @PutMapping("/me/favorites")
    public ResponseEntity<Void> replaceFavorites(
        @Valid @RequestBody FavoritesRequest request
    ) {
        service.replaceFavorites(request);
        return ResponseEntity.noContent().build();
    }

    // ---------- onboarding ----------
    @GetMapping("/username-available")
    public ResponseEntity<UsernameAvailabilityResponse> checkUsernameAvailable(
        @RequestParam String username
    ) {
        return ResponseEntity.ok(service.checkUsernameAvailable(username));
    }

    // ---------- perfil ----------
    @GetMapping("/{username}")
    public ResponseEntity<?> getProfile(
        @PathVariable String username,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return ResponseEntity.ok(service.getProfile(username, language));
    }

    // ---------- busqueda ----------
    @GetMapping("/search")
    public ResponseEntity<Page<ProfileSummaryResponse>> searchProfiles(
        @RequestParam String q,
        Pageable pageable
    ) {
        return ResponseEntity.ok(service.searchProfiles(q, pageable));
    }

    // ---------- seguidores y seguidos ----------
    @PostMapping("/{username}/follow")
    public ResponseEntity<Void> follow(@PathVariable String username) {
        service.follow(username);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{username}/follow")
    public ResponseEntity<Void> unfollow(@PathVariable String username) {
        service.unfollow(username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/followers")
    public ResponseEntity<Page<ProfileSummaryResponse>> getMyFollowers(
        Pageable pageable
    ) {
        return ResponseEntity.ok(service.getMyFollowers(pageable));
    }

    @GetMapping("/me/following")
    public ResponseEntity<Page<ProfileSummaryResponse>> getMyFollowing(
        Pageable pageable
    ) {
        return ResponseEntity.ok(service.getMyFollowing(pageable));
    }

    @GetMapping("/me/follow-requests")
    public ResponseEntity<Page<ProfileSummaryResponse>> getFollowRequests(
        Pageable pageable
    ) {
        return ResponseEntity.ok(service.getFollowRequests(pageable));
    }

    @PostMapping("/me/follow-requests/{username}")
    public ResponseEntity<Void> acceptFollowRequest(
        @PathVariable String username
    ) {
        service.acceptFollowRequest(username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/follow-requests/{username}")
    public ResponseEntity<Void> rejectFollowRequest(
        @PathVariable String username
    ) {
        service.rejectFollowRequest(username);
        return ResponseEntity.noContent().build();
    }
}
