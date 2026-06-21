package com.watchnext.list_service.dto;

import com.watchnext.list_service.entity.ListVisibility;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserListResponse {

    private UUID id;
    private String name;
    private String description;
    private ListVisibility visibility;
    private Integer itemCount; // agregamos una cantidad de items para el front
    private Instant createdAt;
    private Instant updatedAt;
}
