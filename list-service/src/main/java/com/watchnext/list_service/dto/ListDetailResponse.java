package com.watchnext.list_service.dto;

import com.watchnext.common.dto.ContentRefResponse;
import com.watchnext.list_service.entity.ListVisibility;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ListDetailResponse {

    private UUID id;
    private String name;
    private String description;
    private ListVisibility visibility;
    private List<ContentRefResponse> items;
    private Instant createdAt;
    private Instant updatedAt;
}
