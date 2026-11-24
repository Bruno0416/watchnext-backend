package com.watchnext.user_service.controller;

import com.watchnext.user_service.dto.AdminProfileResponse;
import com.watchnext.user_service.dto.InternalIdentityRequest;
import com.watchnext.user_service.service.admin.AdminProfileService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// rutas internas que usa el panel de admin a traves de auth-service
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users/internal/admin")
public class InternalAdminController {

    private final AdminProfileService service;

    @PostMapping("/profiles")
    public ResponseEntity<List<AdminProfileResponse>> getProfiles(@RequestBody List<String> authUserIds) {
        return ResponseEntity.ok(service.getProfiles(authUserIds));
    }

    @PutMapping("/{authUserId}/identity")
    public ResponseEntity<AdminProfileResponse> updateIdentity(
        @PathVariable String authUserId,
        @Valid @RequestBody InternalIdentityRequest request
    ) {
        return ResponseEntity.ok(service.updateIdentity(authUserId, request));
    }

    @DeleteMapping("/{authUserId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String authUserId) {
        service.deleteProfile(authUserId);
        return ResponseEntity.noContent().build();
    }
}
