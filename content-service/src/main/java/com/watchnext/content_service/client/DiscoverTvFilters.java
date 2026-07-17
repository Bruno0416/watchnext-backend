package com.watchnext.content_service.client;

import java.time.LocalDate;

// parametros opcionales de /discover/tv, cualquier campo null se omite del query (ver fetchTvList)
public record DiscoverTvFilters(
    String withGenres,
    String sortBy,
    String withType,
    String withoutGenres,
    Integer voteCountGte,
    LocalDate airDateGte,
    LocalDate airDateLte
) {

    public static DiscoverTvFilters of(String withGenres, String sortBy) {
        // 1. construir filtros con solo genero y orden, resto null
        return new DiscoverTvFilters(withGenres, sortBy, null, null, null, null, null);
    }
}
