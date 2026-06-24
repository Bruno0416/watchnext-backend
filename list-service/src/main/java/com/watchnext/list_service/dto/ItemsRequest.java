package com.watchnext.list_service.dto;

import com.watchnext.common.dto.ContentRefRequest;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ItemsRequest(
    @NotEmpty(message = "Los items no pueden estar vacíos")
    List<ContentRefRequest> items
) {}
