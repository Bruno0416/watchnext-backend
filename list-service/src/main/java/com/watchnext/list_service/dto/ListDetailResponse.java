package com.watchnext.list_service.dto;

import com.watchnext.common.dto.internal.ContentBasicDetail;
import com.watchnext.list_service.entity.ListVisibility;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ListDetailResponse(
    UUID id,
    String name,
    String description,
    ListVisibility visibility,
    List<ContentBasicDetail> items,
    Instant createdAt,
    Instant updatedAt
) {}
