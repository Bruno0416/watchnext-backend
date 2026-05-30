package com.watchnext.content_service.service;

import com.watchnext.content_service.client.TmdbClient;
import com.watchnext.content_service.dto.movies.MovieDetails;
import com.watchnext.content_service.dto.movies.MovieListResponse;
import com.watchnext.content_service.dto.tv.TvDetails;
import com.watchnext.content_service.dto.tv.TvListResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private static final Duration DETAILS_TIME = Duration.ofHours(24);
    private static final Duration LISTS_TIME = Duration.ofHours(5);

    private final TmdbClient tmdbClient;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    // ----------> Movies <----------

    @Override
    public Mono<MovieDetails> getMovieDetails(
        Integer movieId,
        String language
    ) {
        return cacheOrFetch(
            "movie:details:" + movieId + ":" + language,
            MovieDetails.class,
            DETAILS_TIME,
            tmdbClient.getMovieDetails(movieId, language)
        );
    }

    @Override
    public Mono<MovieListResponse> getNowPlayingMovies(
        Integer page,
        String language
    ) {
        return cacheOrFetch(
            "movie:now_playing:" + page + ":" + language,
            MovieListResponse.class,
            LISTS_TIME,
            tmdbClient.getNowPlaying(page, language)
        );
    }

    @Override
    public Mono<MovieListResponse> getPopularMovies(
        Integer page,
        String language
    ) {
        return cacheOrFetch(
            "movie:popular:" + page + ":" + language,
            MovieListResponse.class,
            LISTS_TIME,
            tmdbClient.getPopular(page, language)
        );
    }

    @Override
    public Mono<MovieListResponse> getTopRatedMovies(
        Integer page,
        String language
    ) {
        return cacheOrFetch(
            "movie:top_rated:" + page + ":" + language,
            MovieListResponse.class,
            LISTS_TIME,
            tmdbClient.getTopRated(page, language)
        );
    }

    @Override
    public Mono<MovieListResponse> getUpcomingMovies(
        Integer page,
        String language
    ) {
        return cacheOrFetch(
            "movie:upcoming:" + page + ":" + language,
            MovieListResponse.class,
            LISTS_TIME,
            tmdbClient.getUpcoming(page, language)
        );
    }

    // ----------> TV Series <----------

    @Override
    public Mono<TvDetails> getTvDetails(Integer tvId, String language) {
        return cacheOrFetch(
            "tv:details:" + tvId + ":" + language,
            TvDetails.class,
            DETAILS_TIME,
            tmdbClient.getTvDetails(tvId, language)
        );
    }

    @Override
    public Mono<TvListResponse> getOnTheAir(Integer page, String language) {
        return cacheOrFetch(
            "tv:on_the_air:" + page + ":" + language,
            TvListResponse.class,
            LISTS_TIME,
            tmdbClient.getOnTheAirTv(page, language)
        );
    }

    @Override
    public Mono<TvListResponse> getPopularTv(Integer page, String language) {
        return cacheOrFetch(
            "tv:popular:" + page + ":" + language,
            TvListResponse.class,
            LISTS_TIME,
            tmdbClient.getPopularTv(page, language)
        );
    }

    @Override
    public Mono<TvListResponse> getTopRatedTv(Integer page, String language) {
        return cacheOrFetch(
            "tv:top_rated:" + page + ":" + language,
            TvListResponse.class,
            LISTS_TIME,
            tmdbClient.getTopRatedTv(page, language)
        );
    }

    // Helper method
    private <T> Mono<T> cacheOrFetch(
        String cacheKey,
        Class<T> type,
        Duration ttl,
        Mono<T> fetch
    ) {
        return redisTemplate
            .opsForValue()
            .get(cacheKey)
            .cast(type)
            .onErrorResume(e -> Mono.empty())
            .switchIfEmpty(
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
