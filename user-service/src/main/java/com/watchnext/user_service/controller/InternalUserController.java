package com.watchnext.user_service.controller;

import com.watchnext.common.context.UserContext;
import com.watchnext.common.dto.internal.PageResponse;
import com.watchnext.user_service.dto.InternalFollowingResponse;
import com.watchnext.user_service.dto.ProfileSummaryResponse;
import com.watchnext.user_service.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users/internal")
public class InternalUserController {

    private final UserService service;

    // ---------- consultas internas ----------
    @GetMapping("/{authUserId}/following")
    public ResponseEntity<InternalFollowingResponse> getFollowing(
        @PathVariable String authUserId
    ) {
        return ResponseEntity.ok(service.getFollowingUserIds(authUserId));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<ProfileSummaryResponse>> bulkGetProfiles(
        @RequestBody List<String> authUserIds
    ) {
        return ResponseEntity.ok(service.bulkGetProfiles(authUserIds));
    }

    // ---------- busqueda ----------
    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProfileSummaryResponse>> searchPublicProfiles(
        @RequestParam String q,
        Pageable pageable
    ) {
        // 1. buscar solo perfiles publicos y mapear a pageresponse evitando page de spring data
        Page<ProfileSummaryResponse> result = service.searchPublicProfiles(q, pageable);
        PageResponse<ProfileSummaryResponse> response = PageResponse.of(
            result.getContent(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }

    // ---------- codigo pais ----------
    @GetMapping("/{userId}/country")
        public ResponseEntity<String> getUserCountry(@PathVariable String userId) {


            return ResponseEntity.ok(service.findCountryByUserId(userId));
        }

    // ---------- user context (country + region) ----------
    @GetMapping("/{userId}/context")
    public ResponseEntity<UserContext> getUserContext(@PathVariable String userId) {
        return ResponseEntity.ok(service.getUserContext(userId));
    }
}
