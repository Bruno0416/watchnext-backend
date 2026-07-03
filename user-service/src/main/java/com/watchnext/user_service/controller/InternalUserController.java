package com.watchnext.user_service.controller;

import com.watchnext.user_service.dto.InternalFollowingResponse;
import com.watchnext.user_service.dto.ProfileSummaryResponse;
import com.watchnext.user_service.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/internal/users")
public class InternalUserController {

    private final UserService service;

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
}
