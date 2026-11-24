package com.watchnext.user_service.service.social;

import com.watchnext.user_service.dto.ProfileSummaryResponse;
import com.watchnext.user_service.dto.RelationshipResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SocialQueryService {
    Page<ProfileSummaryResponse> getFollowers(String username, Pageable pageable);

    Page<ProfileSummaryResponse> getFollowing(String username, Pageable pageable);

    RelationshipResponse getRelationship(String username);
}
