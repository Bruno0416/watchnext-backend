package com.watchnext.list_service.dto;

import com.watchnext.list_service.entity.ListVisibility;
import java.time.Instant;
import java.util.UUID;

public record UserListResponse(
    UUID id,
    String name,
    String description,
    ListVisibility visibility,
    Integer itemCount,
    Instant createdAt,
    Instant updatedAt
) {}
