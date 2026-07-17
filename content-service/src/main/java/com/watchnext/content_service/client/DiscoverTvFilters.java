package com.watchnext.content_service.client;

import java.time.LocalDate;

// 1. Parametros opcionales de /discover/tv. Cualquier campo null se omite del query (ver fetchTvList).
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
        return new DiscoverTvFilters(withGenres, sortBy, null, null, null, null, null);
    }
}
