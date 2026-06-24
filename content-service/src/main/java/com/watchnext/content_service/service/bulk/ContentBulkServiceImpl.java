package com.watchnext.content_service.service.bulk;

import com.watchnext.common.dto.ContentRefRequest;
import com.watchnext.common.dto.internal.ContentBasicDetail;
import com.watchnext.common.model.MediaType;
import com.watchnext.content_service.client.TmdbClient;
import com.watchnext.content_service.dto.movies.MovieDetails;
import com.watchnext.content_service.dto.tv.TvDetails;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ContentBulkServiceImpl implements ContentBulkService {

    private static final Duration DETAILS_TIME = Duration.ofHours(24);
    private static final int CONCURRENCY = 8; // tope para no pasar el rate limit de TMDB

    private final TmdbClient tmdbClient;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    @Override
    public Mono<List<ContentBasicDetail>> fetchBulkContent(
        List<ContentRefRequest> requests,
        String language
    ) {
        // 1. recorrer lista de peticiones eliminando duplicados
        return Flux.fromIterable(requests)
            .distinct()
            // 2. procesar en paralelo respetando el limite de concurrencia
            .flatMapSequential(ref -> fetchOne(ref, language), CONCURRENCY)
            .collectList();
    }

    private Mono<ContentBasicDetail> fetchOne(
        ContentRefRequest ref,
        String language
    ) {
        Integer id = ref.tmdbId().intValue();

        // 1. procesar busqueda segun el tipo de contenido
        if (ref.mediaType() == MediaType.MOVIE) {
            String key = "content:movie:" + id + ":" + language;
            // 2. buscar pelicula en cache o tmdb y mapear a objeto basico
            return cacheOrFetch(
                key,
                MovieDetails.class,
                DETAILS_TIME,
                tmdbClient.getMovieDetails(id, language)
            )
                .map(this::toBasic)
                .onErrorResume(e -> Mono.empty());
        } else {
            String key = "content:tv:" + id + ":" + language;
            // 2. buscar serie en cache o tmdb y mapear a objeto basico
            return cacheOrFetch(
                key,
                TvDetails.class,
                DETAILS_TIME,
                tmdbClient.getTvDetails(id, language)
            )
                .map(this::toBasic)
                .onErrorResume(e -> Mono.empty());
        }
    }

    private ContentBasicDetail toBasic(MovieDetails m) {
        return new ContentBasicDetail(
            m.id(),
            MediaType.MOVIE,
            m.title(),
            m.posterPath(),
            m.voteAverage(),
            m.releaseDate(),
            m.runtime(),
            null
        );
    }

    private ContentBasicDetail toBasic(TvDetails t) {
        return new ContentBasicDetail(
            t.id(),
            MediaType.TV,
            t.name(),
            t.posterPath(),
            t.voteAverage(),
            t.firstAirDate(),
            null,
            t.numberOfSeasons()
        );
    }

    private <T> Mono<T> cacheOrFetch(
        String cacheKey,
        Class<T> type,
        Duration ttl,
        Mono<T> fetch
    ) {
        // 1. intentar leer desde cache
        return redisTemplate
            .opsForValue()
            .get(cacheKey)
            .cast(type)
            .onErrorResume(e -> Mono.empty())
            .switchIfEmpty(
                // 2. si falla o no existe, hacer peticion y cachear
                fetch.flatMap(value ->
                    redisTemplate
                        .opsForValue()
                        .set(cacheKey, value, ttl)
                        .onErrorComplete()
                        .thenReturn(value)
                )
            );
    }
}
