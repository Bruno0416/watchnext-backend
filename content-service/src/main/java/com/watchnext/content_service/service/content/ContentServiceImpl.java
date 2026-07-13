package com.watchnext.content_service.service.content;

import com.watchnext.common.enums.MediaType;
import com.watchnext.common.enums.TimeWindow;
import com.watchnext.content_service.client.TmdbClient;
import com.watchnext.content_service.config.TmdbProperties;
import com.watchnext.content_service.dto.common.CastMember;
import com.watchnext.content_service.dto.common.CrewMember;
import com.watchnext.content_service.dto.common.Genre;
import com.watchnext.content_service.dto.common.GenreListResponse;
import com.watchnext.content_service.dto.common.MediaSummary;
import com.watchnext.content_service.dto.common.ReviewResponse;
import com.watchnext.content_service.dto.common.TrendingResponse;
import com.watchnext.content_service.dto.common.Video;
import com.watchnext.content_service.dto.common.WatchProviders;
import com.watchnext.content_service.dto.movies.CollectionDetails;
import com.watchnext.content_service.dto.movies.MovieDetails;
import com.watchnext.content_service.dto.movies.MovieDetailsRaw;
import com.watchnext.content_service.dto.movies.MovieListResponse;
import com.watchnext.content_service.dto.persons.PersonCredit;
import com.watchnext.content_service.dto.persons.PersonDetails;
import com.watchnext.content_service.dto.persons.PersonDetailsRaw;
import com.watchnext.content_service.dto.persons.PersonImage;
import com.watchnext.content_service.dto.tv.TvDetails;
import com.watchnext.content_service.dto.tv.TvDetailsRaw;
import com.watchnext.content_service.dto.tv.TvEpisode;
import com.watchnext.content_service.dto.tv.TvListResponse;
import com.watchnext.content_service.dto.tv.TvSeasonDetail;
import com.watchnext.content_service.util.ContentEnrichment;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
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
    private static final Duration TRENDING_TIME = Duration.ofHours(1);
    private static final Duration GENRES_TIME = Duration.ofDays(7);
    private static final Duration REVIEWS_TIME = Duration.ofHours(12);
    private static final Duration PROVIDERS_TIME = Duration.ofHours(12);
    private static final int FILMOGRAPHY_CAP = 30;

    private final TmdbClient tmdbClient;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final TmdbProperties tmdbProperties;

    // ----------> Movies <----------

    @Override
    public Mono<MovieDetails> getMovieDetails(Integer movieId, String language) {
        return cacheOrFetch(
            "movie:details:" + movieId + ":" + language,
            MovieDetails.class,
            DETAILS_TIME,
            fetchEnrichedMovie(movieId, language)
        );
    }

    @Override
    public Mono<WatchProviders> getMovieWatchProviders(Integer movieId, String region) {
        // 1. resolver la region: si no llega por header, usar el default de config
        String watchRegion = region != null ? region : tmdbProperties.getDefaultRegion();
        return cacheOrFetch(
            "movie:providers:" + movieId + ":" + watchRegion,
            WatchProviders.class,
            PROVIDERS_TIME,
            tmdbClient.getMovieWatchProviders(movieId, watchRegion)
                .onErrorResume(e -> Mono.just(new WatchProviders(null, null, null)))
        );
    }

    private Mono<MovieDetails> fetchEnrichedMovie(Integer movieId, String language) {
        return tmdbClient.getMovieDetails(movieId, language, null).flatMap(raw -> {
            List<CastMember> cast = ContentEnrichment.trimCast(castFrom(raw.credits()));
            List<Video> videos = ContentEnrichment.normalizeVideos(videosFrom(raw.videos()));
            List<CrewMember> directors = extractDirectors(raw.credits());
            List<MediaSummary> recommendations = mapMediaSummaries(raw.recommendations(), "movie");
            List<MediaSummary> similar = mapMediaSummaries(raw.similar(), "movie");
            List<Genre> keywords = raw.keywords() != null ? raw.keywords().keywords() : List.of();
            List<com.watchnext.content_service.dto.common.AlternativeTitle> altTitles =
                raw.alternativeTitles() != null ? raw.alternativeTitles().titles() : List.of();

            if (videos.isEmpty() && !isEnglish(language)) {
                return tmdbClient.getMovieDetails(movieId, "en-US", null)
                    .map(fallback -> buildMovieDetails(raw, cast,
                        ContentEnrichment.normalizeVideos(videosFrom(fallback.videos())),
                        directors, raw.externalIds(), keywords, altTitles,
                        recommendations, similar));
            }
            return Mono.just(buildMovieDetails(raw, cast, videos, directors,
                raw.externalIds(), keywords, altTitles, recommendations, similar));
        });
    }

    private static MovieDetails buildMovieDetails(
        MovieDetailsRaw raw,
        List<CastMember> cast,
        List<Video> videos,
        List<CrewMember> directors,
        com.watchnext.content_service.dto.common.ExternalIds externalIds,
        List<Genre> keywords,
        List<com.watchnext.content_service.dto.common.AlternativeTitle> alternativeTitles,
        List<MediaSummary> recommendations,
        List<MediaSummary> similar
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
            videos,
            directors,
            externalIds,
            keywords,
            alternativeTitles,
            recommendations,
            similar
        );
    }

    @Override
    public Mono<MovieListResponse> getNowPlayingMovies(Integer page, String language) {
        return cacheOrFetch(
            "movie:now_playing:" + page + ":" + language,
            MovieListResponse.class,
            LISTS_TIME,
            tmdbClient.getNowPlaying(page, language)
        );
    }

    @Override
    public Mono<MovieListResponse> getPopularMovies(Integer page, String language) {
        return cacheOrFetch(
            "movie:popular:" + page + ":" + language,
            MovieListResponse.class,
            LISTS_TIME,
            tmdbClient.getPopular(page, language)
        );
    }

    @Override
    public Mono<MovieListResponse> getTopRatedMovies(Integer page, String language) {
        return cacheOrFetch(
            "movie:top_rated:" + page + ":" + language,
            MovieListResponse.class,
            LISTS_TIME,
            tmdbClient.getTopRated(page, language)
        );
    }

    @Override
    public Mono<MovieListResponse> getUpcomingMovies(Integer page, String language) {
        return cacheOrFetch(
            "movie:upcoming:" + page + ":" + language,
            MovieListResponse.class,
            LISTS_TIME,
            tmdbClient.getUpcoming(page, language)
        );
    }

    @Override
    public Mono<MovieListResponse> discoverMovies(
        String genres,
        String sortBy,
        Integer page,
        String language,
        String region
    ) {
        String key = "movie:discover:" +
            (genres != null ? genres : "all") + ":" +
            (sortBy != null ? sortBy : "default") + ":" +
            page + ":" + language + ":" +
            (region != null ? region : "nr");
        return cacheOrFetch(key, MovieListResponse.class, LISTS_TIME,
            tmdbClient.discoverMovies(genres, sortBy, page, language, region));
    }

    @Override
    public Mono<ReviewResponse> getMovieReviews(Integer movieId, Integer page) {
        return cacheOrFetch(
            "movie:reviews:" + movieId + ":" + page,
            ReviewResponse.class,
            REVIEWS_TIME,
            tmdbClient.getMovieReviews(movieId, page)
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

    @Override
    public Mono<WatchProviders> getTvWatchProviders(Integer tvId, String region) {
        // 1. resolver la region: si no llega por header, usar el default de config
        String watchRegion = region != null ? region : tmdbProperties.getDefaultRegion();
        return cacheOrFetch(
            "tv:providers:" + tvId + ":" + watchRegion,
            WatchProviders.class,
            PROVIDERS_TIME,
            tmdbClient.getTvWatchProviders(tvId, watchRegion)
                .onErrorResume(e -> Mono.just(new WatchProviders(null, null, null)))
        );
    }

    private Mono<TvDetails> fetchEnrichedTv(Integer tvId, String language) {
        return tmdbClient.getTvDetails(tvId, language).flatMap(raw -> {
            List<CastMember> cast = ContentEnrichment.trimCast(castFrom(raw.credits()));
            List<Video> videos = ContentEnrichment.normalizeVideos(videosFrom(raw.videos()));
            List<MediaSummary> recommendations = mapTvMediaSummaries(raw.recommendations());
            List<MediaSummary> similar = mapTvMediaSummaries(raw.similar());
            List<Genre> keywords = raw.keywords() != null ? raw.keywords().keywords() : List.of();
            List<com.watchnext.content_service.dto.common.AlternativeTitle> altTitles =
                raw.alternativeTitles() != null ? raw.alternativeTitles().titles() : List.of();

            if (videos.isEmpty() && !isEnglish(language)) {
                return tmdbClient.getTvDetails(tvId, "en-US")
                    .map(fallback -> buildTvDetails(raw, cast,
                        ContentEnrichment.normalizeVideos(videosFrom(fallback.videos())),
                        raw.createdBy(), raw.externalIds(), keywords, altTitles,
                        recommendations, similar));
            }
            return Mono.just(buildTvDetails(raw, cast, videos, raw.createdBy(),
                raw.externalIds(), keywords, altTitles, recommendations, similar));
        });
    }

    private static TvDetails buildTvDetails(
        TvDetailsRaw raw,
        List<CastMember> cast,
        List<Video> videos,
        List<CrewMember> createdBy,
        com.watchnext.content_service.dto.common.ExternalIds externalIds,
        List<Genre> keywords,
        List<com.watchnext.content_service.dto.common.AlternativeTitle> alternativeTitles,
        List<MediaSummary> recommendations,
        List<MediaSummary> similar
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
            videos,
            createdBy,
            externalIds,
            keywords,
            alternativeTitles,
            recommendations,
            similar
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
                .map(raw -> {
                    Double avg = calculateSeasonAverage(raw.episodes());
                    return new TvSeasonDetail(
                        raw.id(), raw.name(), raw.overview(),
                        raw.seasonNumber(), raw.posterPath(),
                        raw.episodes(), avg
                    );
                })
        );
    }

    private static Double calculateSeasonAverage(List<TvEpisode> episodes) {
        if (episodes == null || episodes.isEmpty()) return null;
        double weightedSum = 0;
        int totalVotes = 0;
        for (TvEpisode ep : episodes) {
            if (ep.voteCount() != null && ep.voteCount() > 0 &&
                ep.voteAverage() != null) {
                weightedSum += ep.voteAverage() * ep.voteCount();
                totalVotes += ep.voteCount();
            }
        }
        return totalVotes > 0
            ? Math.round(weightedSum / totalVotes * 10.0) / 10.0
            : null;
    }

    @Override
    public Mono<TvEpisode> getTvEpisodeDetail(
        Integer tvId,
        Integer seasonNumber,
        Integer episodeNumber,
        String language
    ) {
        return cacheOrFetch(
            "tv:episode:" + tvId + ":" + seasonNumber + ":" + episodeNumber + ":" + language,
            TvEpisode.class,
            DETAILS_TIME,
            tmdbClient.getTvEpisode(tvId, seasonNumber, episodeNumber, language)
        );
    }

    @Override
    public Mono<TvListResponse> discoverTv(
        String genres,
        String sortBy,
        Integer page,
        String language,
        String region
    ) {
        String key = "tv:discover:" +
            (genres != null ? genres : "all") + ":" +
            (sortBy != null ? sortBy : "default") + ":" +
            page + ":" + language + ":" +
            (region != null ? region : "nr");
        return cacheOrFetch(key, TvListResponse.class, LISTS_TIME,
            tmdbClient.discoverTv(genres, sortBy, page, language, region));
    }

    @Override
    public Mono<ReviewResponse> getTvReviews(Integer tvId, Integer page) {
        return cacheOrFetch(
            "tv:reviews:" + tvId + ":" + page,
            ReviewResponse.class,
            REVIEWS_TIME,
            tmdbClient.getTvReviews(tvId, page)
        );
    }

    // ----------> Persons <----------

    @Override
    public Mono<PersonDetails> getPersonDetails(Long personId, String language) {
        return cacheOrFetch(
            "person:details:" + personId + ":" + language,
            PersonDetails.class,
            DETAILS_TIME,
            tmdbClient.getPersonDetails(personId, language)
                .map(raw -> {
                    // 1. construir filmografia deduplicada desde cast + crew
                    List<PersonCredit> filmography = buildFilmography(raw.combinedCredits());
                    // 2. aplanar las imagenes anidadas (images.profiles) con fallback vacio
                    List<PersonImage> images = raw.images() != null
                        ? raw.images().profiles()
                        : List.of();
                    // 3. armar el dto de respuesta
                    return new PersonDetails(
                        raw.id(), raw.name(), raw.biography(),
                        raw.birthday(), raw.deathday(), raw.placeOfBirth(),
                        raw.profilePath(), raw.knownForDepartment(),
                        raw.externalIds(), images, filmography
                    );
                })
        );
    }

    // --- Helper privado ---
    private static List<PersonCredit> buildFilmography(PersonDetailsRaw.CombinedCredits credits) {
        // 1. salir temprano si no hay creditos
        if (credits == null) return List.of();
        // 2. unir creditos de actuacion (cast) y de equipo (crew)
        List<PersonCredit> all = new ArrayList<>();
        if (credits.cast() != null) all.addAll(credits.cast());
        if (credits.crew() != null) all.addAll(credits.crew());
        // 3. deduplicar por id, ordenar por popularidad y fecha desc, y limitar
        var seen = new LinkedHashSet<Long>();
        return all.stream()
            .filter(c -> c.id() != null && seen.add(c.id()))
            .sorted(Comparator
                .comparing(PersonCredit::popularity, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(PersonCredit::releaseDate, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(FILMOGRAPHY_CAP)
            .toList();
    }

    // ----------> Genres <----------

    @Override
    public Mono<GenreListResponse> getMovieGenres(String language) {
        return cacheOrFetch(
            "genres:movie:" + language,
            GenreListResponse.class,
            GENRES_TIME,
            tmdbClient.getMovieGenres(language)
        );
    }

    @Override
    public Mono<GenreListResponse> getTvGenres(String language) {
        return cacheOrFetch(
            "genres:tv:" + language,
            GenreListResponse.class,
            GENRES_TIME,
            tmdbClient.getTvGenres(language)
        );
    }

    // ----------> Trending <----------

    @Override
    public Mono<TrendingResponse> getTrending(
        MediaType mediaType,
        TimeWindow timeWindow,
        Integer page
    ) {
        return cacheOrFetch(
            "trending:" + mediaType.name().toLowerCase() + ":" + timeWindow.name().toLowerCase() + ":" + page,
            TrendingResponse.class,
            TRENDING_TIME,
            tmdbClient.getTrending(mediaType, timeWindow, page)
        );
    }

    // ----------> Collections <----------

    @Override
    public Mono<CollectionDetails> getCollection(
        Integer collectionId,
        String language
    ) {
        return cacheOrFetch(
            "collection:" + collectionId + ":" + language,
            CollectionDetails.class,
            DETAILS_TIME,
            tmdbClient.getCollection(collectionId, language)
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

    private List<CrewMember> extractDirectors(
        com.watchnext.content_service.dto.common.Credits credits
    ) {
        if (credits == null || credits.crew() == null) return List.of();
        return credits.crew().stream()
            .filter(c -> "Director".equals(c.job()))
            .toList();
    }

    private List<Video> videosFrom(
        com.watchnext.content_service.dto.common.VideoWrapper videos
    ) {
        return videos == null || videos.results() == null
            ? Collections.emptyList()
            : videos.results();
    }

    private static List<MediaSummary> mapMediaSummaries(
        MovieListResponse response,
        String mediaType
    ) {
        if (response == null || response.results() == null) return List.of();
        return response.results().stream()
            .map(m -> new MediaSummary(
                m.id().longValue(), m.title(), m.posterPath(),
                m.voteAverage(), mediaType, m.releaseDate()))
            .toList();
    }

    private static List<MediaSummary> mapTvMediaSummaries(TvListResponse response) {
        if (response == null || response.results() == null) return List.of();
        return response.results().stream()
            .map(t -> new MediaSummary(
                t.id().longValue(), t.name(), t.posterPath(),
                t.voteAverage(), "tv", t.firstAirDate()))
            .toList();
    }

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
