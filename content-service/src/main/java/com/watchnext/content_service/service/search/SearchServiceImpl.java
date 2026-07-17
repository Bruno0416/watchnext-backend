package com.watchnext.content_service.service.search;

import com.watchnext.content_service.cache.SearchCacheService;
import com.watchnext.content_service.client.TmdbClient;
import com.watchnext.content_service.dto.search.ParsedQuery;
import com.watchnext.content_service.dto.search.SearchResponse;
import com.watchnext.content_service.dto.search.SearchResult;
import com.watchnext.content_service.pipeline.QueryNormalizer;
import com.watchnext.content_service.pipeline.QueryParser;
import com.watchnext.content_service.pipeline.ResultRanker;
import com.watchnext.content_service.pipeline.SpellCorrector;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SearchServiceImpl implements SearchService {

    private final QueryNormalizer normalizer;
    private final QueryParser parser;
    private final SpellCorrector spellCorrector;
    private final ResultRanker ranker;
    private final SearchCacheService cache;
    private final TmdbClient tmdbClient;

    public SearchServiceImpl(
        QueryNormalizer normalizer,
        QueryParser parser,
        SpellCorrector spellCorrector,
        ResultRanker ranker,
        SearchCacheService cache,
        TmdbClient tmdbClient
    ) {
        this.normalizer = normalizer;
        this.parser = parser;
        this.spellCorrector = spellCorrector;
        this.ranker = ranker;
        this.cache = cache;
        this.tmdbClient = tmdbClient;
    }

    // ---------- busqueda ----------

    @Override
    public Mono<SearchResponse> search(String rawQuery, String language) {
        // 1. delegar al metodo con types vacio para busqueda completa
        return search(rawQuery, language, Set.of());
    }

    @Override
    public Mono<SearchResponse> search(String rawQuery, String language, Set<String> types) {
        // 1. normalizar el texto de busqueda original
        String normalized = normalizer.normalize(rawQuery);

        // 2. parsear la busqueda para extraer filtros y limpiar el texto
        ParsedQuery parsed = parser.parse(normalized);

        // 3. validar si despues de limpiar la busqueda quedo vacia
        if (parsed.cleanQuery().isBlank()) {
            return Mono.just(
                new SearchResponse(rawQuery, "", false, 0, List.of())
            );
        }

        // 4. normalizar los types recibidos
        Set<String> normalizedTypes = normalizeTypes(types);

        // 5. buscar resultados en cache o ejecutar la busqueda en tmdb si no existe
        return cache
            .get(parsed, language, normalizedTypes)
            .switchIfEmpty(
                Mono.defer(() -> executeAndCache(rawQuery, parsed, language, normalizedTypes))
            );
    }

    private Mono<SearchResponse> executeAndCache(
        String rawQuery,
        ParsedQuery parsed,
        String language,
        Set<String> types
    ) {
        // 1. determinar si se debe corregir la ortografia
        boolean shouldCorrect = shouldCorrect(parsed, language);

        // 2. aplicar correccion ortografica si es necesario y calcular si hubo correccion
        String executedQuery = shouldCorrect
            ? spellCorrector.correct(parsed.cleanQuery(), language)
            : parsed.cleanQuery();

        boolean corrected = !executedQuery.equals(parsed.cleanQuery());

        // 3. realizar la busqueda en tmdb segun los types ordenar y guardar en cache
        return executeTmdbSearch(executedQuery, parsed, language, types)
            .map(ranked ->
                new SearchResponse(
                    rawQuery,
                    executedQuery,
                    corrected,
                    ranked.size(),
                    ranked
                )
            )
            .flatMap(response -> cache.put(parsed, language, types, response));
    }

    // --- helper privado ---
    private Mono<List<SearchResult>> executeTmdbSearch(
        String query,
        ParsedQuery parsed,
        String language,
        Set<String> types
    ) {
        // 1. asegurar set de types no nulo y filtrar solo los que tmdb soporta
        Set<String> safeTypes = types != null ? types : Set.of();
        Set<String> tmdbTypes = safeTypes.stream()
            .filter(t -> Set.of("movie", "tv", "person").contains(t))
            .collect(java.util.stream.Collectors.toSet());

        // 2. realizar llamada exclusiva a endpoint person
        if (tmdbTypes.equals(Set.of("person"))) {
            return tmdbClient
                .search(query, parsed.year(), "person", language)
                .map(results -> ranker.rank(results, query));
        }

        // 3. realizar llamada completa a endpoint multi
        if (tmdbTypes.isEmpty() || tmdbTypes.contains("person")) {
            return tmdbClient
                .search(query, parsed.year(), null, language)
                .map(results -> ranker.rank(results, query));
        }

        // 4. realizar llamada exclusiva a endpoint movie
        if (tmdbTypes.equals(Set.of("movie"))) {
            return tmdbClient
                .search(query, parsed.year(), "movie", language)
                .map(results -> ranker.rank(results, query));
        }

        // 5. realizar llamada exclusiva a endpoint tv
        if (tmdbTypes.equals(Set.of("tv"))) {
            return tmdbClient
                .search(query, parsed.year(), "tv", language)
                .map(results -> ranker.rank(results, query));
        }

        // 6. ejecutar llamadas paralelas de movie y tv integrando resultados
        Mono<List<SearchResult>> movies = tmdbClient.search(
            query, parsed.year(), "movie", language
        );
        Mono<List<SearchResult>> tvs = tmdbClient.search(
            query, parsed.year(), "tv", language
        );
        return Mono.zip(movies, tvs)
            .map(tuple -> {
                List<SearchResult> combined = Stream.concat(
                    tuple.getT1().stream(),
                    tuple.getT2().stream()
                ).toList();
                return ranker.rank(combined, query);
            });
    }

    private Set<String> normalizeTypes(Set<String> types) {
        // 1. filtrar solo los types validos y devolver un set inmutable
        if (types == null) return Set.of();
        return types.stream()
            .filter(t -> Set.of("movie", "tv", "person").contains(t))
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private boolean shouldCorrect(ParsedQuery parsed, String language) {
        // 1. solo corregir si hay de 1 a 4 palabras y no se especifico año
        return (
            spellCorrector.isReady(language) &&
            parsed.wordCount() >= 1 &&
            parsed.wordCount() <= 4 &&
            !parsed.hasYear()
        );
    }
}
