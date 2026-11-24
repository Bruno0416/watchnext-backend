package com.watchnext.user_service.service.admin;

import com.watchnext.user_service.dto.AdminProfileResponse;
import com.watchnext.user_service.dto.InternalIdentityRequest;
import java.util.List;

public interface AdminProfileService {
    List<AdminProfileResponse> getProfiles(List<String> authUserIds);

    AdminProfileResponse updateIdentity(String authUserId, InternalIdentityRequest request);

    void deleteProfile(String authUserId);
}
