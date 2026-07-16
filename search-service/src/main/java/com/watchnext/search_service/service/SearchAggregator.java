package com.watchnext.search_service.service;

import com.watchnext.common.dto.internal.PageResponse;
import com.watchnext.search_service.client.ContentServiceClient;
import com.watchnext.search_service.client.UserServiceClient;
import com.watchnext.search_service.dto.ContentSearchSection;
import com.watchnext.search_service.dto.SearchType;
import com.watchnext.search_service.dto.UnifiedSearchResponse;
import com.watchnext.search_service.dto.UserSummary;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SearchAggregator {

    private static final Set<SearchType> TMDB_TYPES = Set.of(
        SearchType.MOVIE,
        SearchType.TV,
        SearchType.PERSON
    );

    private final ContentServiceClient contentClient;
    private final UserServiceClient userClient;

    // ---------- agregacion ----------
    public Mono<UnifiedSearchResponse> search(
        String query,
        Set<SearchType> types,
        int page
    ) {
        // 1. determinar que upstreams llamar segun los types solicitados
        boolean needsContent = types.isEmpty()
            || types.stream().anyMatch(TMDB_TYPES::contains);
        boolean needsUsers = types.isEmpty() || types.contains(SearchType.USER);

        // 2. recuperar resultados envueltos en optional emitiendo empty en caso de falla
        Mono<Optional<ContentSearchSection>> contentMono = needsContent
            ? contentClient.search(query, filterTmdbTypes(types), page)
                .map(Optional::of)
                .onErrorReturn(Optional.empty())
            : Mono.just(Optional.empty());

        Mono<Optional<PageResponse<UserSummary>>> usersMono = needsUsers
            ? userClient.search(query, page)
                .map(Optional::of)
                .onErrorReturn(Optional.empty())
            : Mono.just(Optional.empty());

        // 3. ensamblar respuestas concurrentes con sus flags de disponibilidad
        return Mono.zip(contentMono, usersMono)
            .map(t -> new UnifiedSearchResponse(
                t.getT1().orElse(null),
                t.getT2().orElse(null),
                t.getT1().isPresent(),
                t.getT2().isPresent()
            ));
    }

    // --- helper privado ---
    private Set<SearchType> filterTmdbTypes(Set<SearchType> types) {
        // 1. filtrar solo los types que soporta la api de tmdb
        return types.stream()
            .filter(TMDB_TYPES::contains)
            .collect(Collectors.toSet());
    }
}
