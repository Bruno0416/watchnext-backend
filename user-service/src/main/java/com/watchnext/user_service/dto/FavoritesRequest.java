package com.watchnext.user_service.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record FavoritesRequest(
    @NotNull(message = "La lista de favoritos no puede ser nula")
    List<@Valid FavoriteItemRequest> items
) {
    public FavoritesRequest {
        if (items == null) items = List.of();
    }
}
