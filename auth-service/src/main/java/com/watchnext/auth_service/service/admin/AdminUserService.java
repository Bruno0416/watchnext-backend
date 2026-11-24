package com.watchnext.auth_service.service.admin;

import com.watchnext.auth_service.dto.admin.AdminCreateUserRequest;
import com.watchnext.auth_service.dto.admin.AdminIdentityRequest;
import com.watchnext.auth_service.dto.admin.AdminUserResponse;
import java.util.List;
import java.util.UUID;

public interface AdminUserService {
    List<AdminUserResponse> search(String query);

    AdminUserResponse getById(UUID userId);

    AdminUserResponse create(AdminCreateUserRequest request);

    AdminUserResponse updateIdentity(UUID userId, AdminIdentityRequest request);

    void delete(UUID userId, String actorId);
}
