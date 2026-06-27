package com.watchnext.content_service.service.content;

import com.watchnext.content_service.client.TmdbClient;
import com.watchnext.content_service.dto.common.CastMember;
import com.watchnext.content_service.dto.common.Video;
import com.watchnext.content_service.dto.movies.MovieDetails;
import com.watchnext.content_service.dto.movies.MovieDetailsRaw;
import com.watchnext.content_service.dto.movies.MovieListResponse;
import com.watchnext.content_service.dto.tv.TvDetails;
import com.watchnext.content_service.dto.tv.TvDetailsRaw;
import com.watchnext.content_service.dto.tv.TvListResponse;
import com.watchnext.content_service.dto.tv.TvSeasonDetail;
import com.watchnext.content_service.util.ContentEnrichment;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
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
            fetchEnrichedMovie(movieId, language)
        );
    }

    private Mono<MovieDetails> fetchEnrichedMovie(
        Integer movieId,
        String language
    ) {
        return tmdbClient.getMovieDetails(movieId, language).flatMap(raw -> {
            List<CastMember> cast = ContentEnrichment.trimCast(
                castFrom(raw.credits())
            );
            List<Video> videos = ContentEnrichment.normalizeVideos(
                videosFrom(raw.videos())
            );

            if (videos.isEmpty() && !isEnglish(language)) {
                return tmdbClient
                    .getMovieDetails(movieId, "en-US")
                    .map(fallback ->
                        buildMovieDetails(
                            raw,
                            cast,
                            ContentEnrichment.normalizeVideos(
                                videosFrom(fallback.videos())
                            )
                        )
                    );
            }
            return Mono.just(buildMovieDetails(raw, cast, videos));
        });
    }

    private static MovieDetails buildMovieDetails(
        MovieDetailsRaw raw,
        List<CastMember> cast,
        List<Video> videos
    ) {
        return new MovieDetails(
            raw.id(),
            raw.title(),
            raw.originalTitle(),
            raw.overview(),
            raw.posterPath(),
            raw.backdropPath(),
            raw.releaseDate(),
            raw.runtime(),
            raw.voteAverage(),
            raw.voteCount(),
            raw.genres(),
            cast,
            videos
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
            fetchEnrichedTv(tvId, language)
        );
    }

    private Mono<TvDetails> fetchEnrichedTv(Integer tvId, String language) {
        return tmdbClient.getTvDetails(tvId, language).flatMap(raw -> {
            List<CastMember> cast = ContentEnrichment.trimCast(
                castFrom(raw.credits())
            );
            List<Video> videos = ContentEnrichment.normalizeVideos(
                videosFrom(raw.videos())
            );

            if (videos.isEmpty() && !isEnglish(language)) {
                return tmdbClient
                    .getTvDetails(tvId, "en-US")
                    .map(fallback ->
                        buildTvDetails(
                            raw,
                            cast,
                            ContentEnrichment.normalizeVideos(
                                videosFrom(fallback.videos())
                            )
                        )
                    );
            }
            return Mono.just(buildTvDetails(raw, cast, videos));
        });
    }

    private static TvDetails buildTvDetails(
        TvDetailsRaw raw,
        List<CastMember> cast,
        List<Video> videos
    ) {
        return new TvDetails(
            raw.id(),
            raw.name(),
            raw.overview(),
            raw.posterPath(),
            raw.backdropPath(),
            raw.firstAirDate(),
            raw.voteAverage(),
            raw.numberOfEpisodes(),
            raw.numberOfSeasons(),
            raw.status(),
            raw.genres(),
            raw.seasons(),
            cast,
            videos
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

    @Override
    public Mono<TvSeasonDetail> getTvSeasonDetail(
        Integer tvId,
        Integer seasonNumber,
        String language
    ) {
        return cacheOrFetch(
            "tv:season:" + tvId + ":" + seasonNumber + ":" + language,
            TvSeasonDetail.class,
            DETAILS_TIME,
            tmdbClient.getTvSeason(tvId, seasonNumber, language)
        );
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private boolean isEnglish(String language) {
        return language == null || language.startsWith("en");
    }

    private List<CastMember> castFrom(
        com.watchnext.content_service.dto.common.Credits credits
    ) {
        return credits == null || credits.cast() == null
            ? Collections.emptyList()
            : credits.cast();
    }

    private List<Video> videosFrom(
        com.watchnext.content_service.dto.common.VideoWrapper videos
    ) {
        return videos == null || videos.results() == null
            ? Collections.emptyList()
            : videos.results();
    }

    private <T> Mono<T> cacheOrFetch(
        String cacheKey,
        Class<T> type,
        Duration ttl,
        Mono<T> fetch
    ) {
        // 1. intentar obtener valor desde redis
        return redisTemplate
            .opsForValue()
            .get(cacheKey)
            .cast(type)
            .onErrorResume(e -> Mono.empty())
            .switchIfEmpty(
                // 2. si no existe, ejecutar peticion y guardar en cache con el ttl proporcionado
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
