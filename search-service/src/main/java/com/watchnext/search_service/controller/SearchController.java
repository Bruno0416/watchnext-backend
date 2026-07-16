package com.watchnext.search_service.controller;

import com.watchnext.search_service.dto.SearchType;
import com.watchnext.search_service.dto.UnifiedSearchResponse;
import com.watchnext.search_service.service.SearchAggregator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/search")
public class SearchController {

    private final SearchAggregator aggregator;

    // ---------- busqueda ----------
    @GetMapping
    public Mono<UnifiedSearchResponse> search(
        @RequestParam("q") String query,
        @RequestParam(value = "types", required = false) Set<SearchType> types,
        @RequestParam(value = "page", defaultValue = "1") int page
    ) {
        // 1. parsear types a enum vacio si no se especifican
        Set<SearchType> resolvedTypes = types == null ? Set.of() : types;

        // 2. forzar busqueda exclusiva de perfiles si empieza con @
        String resolvedQuery = query;
        if (query != null && query.startsWith("@")) {
            resolvedQuery = query.substring(1).trim();
            resolvedTypes = Set.of(SearchType.USER);
        }

        // 3. delegar al agregador para realizar la busqueda unificada
        return aggregator.search(resolvedQuery, resolvedTypes, page);
    }
}
