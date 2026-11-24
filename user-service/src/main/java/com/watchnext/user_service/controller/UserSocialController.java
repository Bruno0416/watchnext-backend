package com.watchnext.user_service.controller;

import com.watchnext.user_service.dto.ProfileSummaryResponse;
import com.watchnext.user_service.dto.RelationshipResponse;
import com.watchnext.user_service.service.social.SocialQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// seguidores, seguidos y relacion con cualquier perfil (los de /me siguen en UserController)
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
public class UserSocialController {

    private final SocialQueryService service;

    @GetMapping("/{username}/followers")
    public ResponseEntity<Page<ProfileSummaryResponse>> getFollowers(
        @PathVariable String username,
        Pageable pageable
    ) {
        return ResponseEntity.ok(service.getFollowers(username, pageable));
    }

    @GetMapping("/{username}/following")
    public ResponseEntity<Page<ProfileSummaryResponse>> getFollowing(
        @PathVariable String username,
        Pageable pageable
    ) {
        return ResponseEntity.ok(service.getFollowing(username, pageable));
    }

    @GetMapping("/{username}/relationship")
    public ResponseEntity<RelationshipResponse> getRelationship(@PathVariable String username) {
        return ResponseEntity.ok(service.getRelationship(username));
    }
}
