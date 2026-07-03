package com.watchnext.user_service.dto;

import com.watchnext.common.dto.ContentRefRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record FavoritesRequest(
    @NotNull(message = "La lista de favoritos no puede ser nula")
    List<@Valid ContentRefRequest> items
) {
    public FavoritesRequest {
        if (items == null) items = List.of();
    }
}
