package com.watchnext.content_service.service.search;

import com.watchnext.content_service.cache.SearchCacheService;
import com.watchnext.content_service.client.TmdbClient;
import com.watchnext.content_service.dto.search.ParsedQuery;
import com.watchnext.content_service.dto.search.SearchResponse;
import com.watchnext.content_service.pipeline.QueryNormalizer;
import com.watchnext.content_service.pipeline.QueryParser;
import com.watchnext.content_service.pipeline.ResultRanker;
import com.watchnext.content_service.pipeline.SpellCorrector;
import java.util.List;
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

    @Override
    public Mono<SearchResponse> search(String rawQuery, String language) {
        // 1. normalizar el texto de busqueda original
        String normalized = normalizer.normalize(rawQuery);
        
        // 2. parsear la busqueda para extraer filtros y limpiar el texto
        ParsedQuery parsed = parser.parse(normalized);

        // 3. validar si despues de limpiar, la busqueda quedo vacia
        if (parsed.cleanQuery().isBlank()) {
            return Mono.just(
                new SearchResponse(rawQuery, "", false, 0, List.of())
            );
        }

        // 4. buscar resultados en cache o ejecutar la busqueda en tmdb si no existe
        return cache
            .get(parsed, language)
            .switchIfEmpty(
                Mono.defer(() -> executeAndCache(rawQuery, parsed, language))
            );
    }

    private Mono<SearchResponse> executeAndCache(
        String rawQuery,
        ParsedQuery parsed,
        String language
    ) {
        // 1. determinar si se debe corregir la ortografia
        boolean shouldCorrect = shouldCorrect(parsed, language);
        
        // 2. aplicar correccion ortografica si es necesario
        String executedQuery = shouldCorrect
            ? spellCorrector.correct(parsed.cleanQuery(), language)
            : parsed.cleanQuery();

        boolean corrected = !executedQuery.equals(parsed.cleanQuery());

        // 3. realizar la busqueda en tmdb, ordenar resultados y guardar en cache
        return tmdbClient
            .search(executedQuery, parsed.year(), parsed.mediaType(), language)
            .map(results -> ranker.rank(results, executedQuery))
            .map(ranked ->
                new SearchResponse(
                    rawQuery,
                    executedQuery,
                    corrected,
                    ranked.size(),
                    ranked
                )
            )
            .flatMap(response -> cache.put(parsed, language, response));
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
