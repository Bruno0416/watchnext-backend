package com.watchnext.content_service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchnext.common.enums.MediaType;
import com.watchnext.common.enums.TimeWindow;
import com.watchnext.content_service.dto.common.GenreListResponse;
import com.watchnext.content_service.dto.common.ReviewResponse;
import com.watchnext.content_service.dto.common.TrendingResponse;
import com.watchnext.content_service.dto.movies.CollectionDetails;
import com.watchnext.content_service.dto.movies.MovieDetailsRaw;
import com.watchnext.content_service.dto.movies.MovieListResponse;
import com.watchnext.content_service.dto.persons.PersonDetailsRaw;
import com.watchnext.content_service.dto.search.SearchResult;
import com.watchnext.content_service.dto.tv.TvDetailsRaw;
import com.watchnext.content_service.dto.tv.TvEpisode;
import com.watchnext.content_service.dto.tv.TvListResponse;
import com.watchnext.content_service.dto.tv.TvSeasonDetail;
import com.watchnext.content_service.exceptions.ErrorFetchingMovieDetails;
import com.watchnext.content_service.exceptions.ErrorFetchingMovieList;
import com.watchnext.content_service.exceptions.ErrorFetchingTvDetails;
import com.watchnext.content_service.exceptions.ErrorFetchingTvList;
import com.watchnext.content_service.exceptions.TmdbResourceNotFound;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Component
public class TmdbClient {

    private static final String MOVIE_APPEND =
        "credits,videos,recommendations,similar,external_ids,keywords,alternative_titles";
    private static final String TV_APPEND =
        "credits,videos,recommendations,similar,external_ids,keywords,alternative_titles";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // ---------- configuracion ----------
    public TmdbClient(
        WebClient.Builder builder,
        @Value("${tmdb.api.base-url}") String baseUrl,
        @Value("${tmdb.api.key}") String apiKey,
        ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;

        HttpClient httpClient = HttpClient.create().responseTimeout(
            Duration.ofSeconds(10)
        );

        this.webClient = builder
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();
    }

    // ---------- peliculas ----------
    public Mono<MovieDetailsRaw> getMovieDetails(
        Integer movieId,
        String language,
        String region
    ) {
        // 1. ejecutar llamada a tmdb controlando timeouts y estados de error 404
        return webClient
            .get()
            .uri(uriBuilder -> {
                uriBuilder
                    .path("/movie/{id}")
                    .queryParam("append_to_response", MOVIE_APPEND)
                    .queryParam("language", language);
                if (region != null) {
                    uriBuilder.queryParam("region", region);
                }
                return uriBuilder.build(movieId);
            })
            .retrieve()
            .onStatus(
                status -> status.value() == 404,
                response ->
                    Mono.error(
                        new TmdbResourceNotFound(
                            "Pelicula con id " + movieId + " no encontrada"
                        )
                    )
            )
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(
                    new ErrorFetchingMovieDetails(
                        "Error al obtener detalles de la pelicula con id " +
                            movieId
                    )
                )
            )
            .bodyToMono(MovieDetailsRaw.class);
    }

    public Mono<MovieListResponse> getNowPlaying(
        Integer page,
        String language
    ) {
        return fetchMovieList("/movie/now_playing", page, language, null);
    }

    public Mono<MovieListResponse> getPopular(Integer page, String language) {
        return fetchMovieList("/movie/popular", page, language, null);
    }

    public Mono<MovieListResponse> getTopRated(Integer page, String language) {
        return fetchMovieList("/movie/top_rated", page, language, null);
    }

    public Mono<MovieListResponse> getUpcoming(Integer page, String language) {
        return fetchMovieList("/movie/upcoming", page, language, null);
    }

    public Mono<MovieListResponse> discoverMovies(
        String genres,
        String sortBy,
        Integer page,
        String language,
        String region
    ) {
        return fetchMovieList("/discover/movie", page, language, region,
            uriBuilder -> {
                if (genres != null && !genres.isBlank()) {
                    uriBuilder.queryParam("with_genres", genres);
                }
                if (sortBy != null && !sortBy.isBlank()) {
                    uriBuilder.queryParam("sort_by", sortBy);
                }
            });
    }

    private Mono<MovieListResponse> fetchMovieList(
        String path,
        Integer page,
        String language,
        String region
    ) {
        return fetchMovieList(path, page, language, region, null);
    }

    private Mono<MovieListResponse> fetchMovieList(
        String path,
        Integer page,
        String language,
        String region,
        java.util.function.Consumer<
            org.springframework.web.util.UriBuilder
        > extraParams
    ) {
        return webClient
            .get()
            .uri(uriBuilder -> {
                uriBuilder
                    .path(path)
                    .queryParam("language", language)
                    .queryParam("page", page);
                if (region != null) {
                    uriBuilder.queryParam("region", region);
                }
                if (extraParams != null) {
                    extraParams.accept(uriBuilder);
                }
                return uriBuilder.build();
            })
            .retrieve()
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(
                    new ErrorFetchingMovieList(
                        "Error al obtener lista de peliculas desde TMDB"
                    )
                )
            )
            .bodyToMono(MovieListResponse.class);
    }

    public Mono<ReviewResponse> getMovieReviews(
        Integer movieId,
        Integer page
    ) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/movie/{id}/reviews")
                    .queryParam("page", page)
                    .build(movieId)
            )
            .retrieve()
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(
                    new ErrorFetchingMovieDetails(
                        "Error al obtener reviews de la pelicula " + movieId
                    )
                )
            )
            .bodyToMono(ReviewResponse.class);
    }

    // --- TV Series ----

    public Mono<TvDetailsRaw> getTvDetails(Integer tvId, String language) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/tv/{id}")
                    .queryParam("append_to_response", TV_APPEND)
                    .queryParam("language", language)
                    .build(tvId)
            )
            .retrieve()
            .onStatus(
                status -> status.value() == 404,
                response ->
                    Mono.error(
                        new TmdbResourceNotFound(
                            "Serie con id " + tvId + " no encontrada"
                        )
                    )
            )
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(
                    new ErrorFetchingTvDetails(
                        "Error al obtener detalles de la serie con id " + tvId
                    )
                )
            )
            .bodyToMono(TvDetailsRaw.class);
    }

    public Mono<TvSeasonDetail> getTvSeason(
        Integer tvId,
        Integer seasonNumber,
        String language
    ) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/tv/{id}/season/{season_number}")
                    .queryParam("language", language)
                    .build(tvId, seasonNumber)
            )
            .retrieve()
            .onStatus(
                status -> status.value() == 404,
                response ->
                    Mono.error(
                        new TmdbResourceNotFound(
                            "Temporada " +
                                seasonNumber +
                                " de la serie " +
                                tvId +
                                " no encontrada"
                        )
                    )
            )
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(
                    new ErrorFetchingTvDetails(
                        "Error al obtener temporada " +
                            seasonNumber +
                            " de la serie " +
                            tvId
                    )
                )
            )
            .bodyToMono(TvSeasonDetail.class);
    }

    public Mono<TvEpisode> getTvEpisode(
        Integer tvId,
        Integer seasonNumber,
        Integer episodeNumber,
        String language
    ) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/tv/{id}/season/{sn}/episode/{en}")
                    .queryParam("append_to_response", "credits")
                    .queryParam("language", language)
                    .build(tvId, seasonNumber, episodeNumber)
            )
            .retrieve()
            .onStatus(
                status -> status.value() == 404,
                response ->
                    Mono.error(
                        new TmdbResourceNotFound(
                            "Episodio " + episodeNumber +
                                " de la temporada " + seasonNumber +
                                " de la serie " + tvId + " no encontrado"
                        )
                    )
            )
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(
                    new ErrorFetchingTvDetails(
                        "Error al obtener episodio " + episodeNumber +
                            " de la temporada " + seasonNumber
                    )
                )
            )
            .bodyToMono(TvEpisode.class);
    }

    public Mono<TvListResponse> getOnTheAirTv(Integer page, String language) {
        return fetchTvList("/tv/on_the_air", page, language);
    }

    public Mono<TvListResponse> getPopularTv(Integer page, String language) {
        return fetchTvList("/tv/popular", page, language);
    }

    public Mono<TvListResponse> getTopRatedTv(Integer page, String language) {
        return fetchTvList("/tv/top_rated", page, language);
    }

    public Mono<TvListResponse> discoverTv(
        String genres,
        String sortBy,
        Integer page,
        String language,
        String region
    ) {
        return fetchTvList("/discover/tv", page, language, region,
            uriBuilder -> {
                if (genres != null && !genres.isBlank()) {
                    uriBuilder.queryParam("with_genres", genres);
                }
                if (sortBy != null && !sortBy.isBlank()) {
                    uriBuilder.queryParam("sort_by", sortBy);
                }
            });
    }

    private Mono<TvListResponse> fetchTvList(
        String path,
        Integer page,
        String language
    ) {
        return fetchTvList(path, page, language, null, null);
    }

    private Mono<TvListResponse> fetchTvList(
        String path,
        Integer page,
        String language,
        String region,
        java.util.function.Consumer<
            org.springframework.web.util.UriBuilder
        > extraParams
    ) {
        return webClient
            .get()
            .uri(uriBuilder -> {
                uriBuilder
                    .path(path)
                    .queryParam("language", language)
                    .queryParam("page", page);
                if (region != null) {
                    uriBuilder.queryParam("region", region);
                }
                if (extraParams != null) {
                    extraParams.accept(uriBuilder);
                }
                return uriBuilder.build();
            })
            .retrieve()
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(
                    new ErrorFetchingTvList(
                        "Error al obtener lista de series desde TMDB"
                    )
                )
            )
            .bodyToMono(TvListResponse.class);
    }

    public Mono<ReviewResponse> getTvReviews(
        Integer tvId,
        Integer page
    ) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/tv/{id}/reviews")
                    .queryParam("page", page)
                    .build(tvId)
            )
            .retrieve()
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(
                    new ErrorFetchingTvDetails(
                        "Error al obtener reviews de la serie " + tvId
                    )
                )
            )
            .bodyToMono(ReviewResponse.class);
    }

    // --- Persons ---

    public Mono<PersonDetailsRaw> getPersonDetails(
        Long personId,
        String language
    ) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/person/{id}")
                    .queryParam("append_to_response", "combined_credits,external_ids,images")
                    .queryParam("language", language)
                    .build(personId)
            )
            .retrieve()
            .onStatus(
                status -> status.value() == 404,
                response ->
                    Mono.error(
                        new TmdbResourceNotFound(
                            "Persona con id " + personId + " no encontrada"
                        )
                    )
            )
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(
                    new ErrorFetchingMovieDetails(
                        "Error al obtener persona con id " + personId
                    )
                )
            )
            .bodyToMono(PersonDetailsRaw.class);
    }

    // --- Genres ---

    public Mono<GenreListResponse> getMovieGenres(String language) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/genre/movie/list")
                    .queryParam("language", language)
                    .build()
            )
            .retrieve()
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(new RuntimeException("Error al obtener generos de peliculas"))
            )
            .bodyToMono(GenreListResponse.class);
    }

    public Mono<GenreListResponse> getTvGenres(String language) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/genre/tv/list")
                    .queryParam("language", language)
                    .build()
            )
            .retrieve()
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(new RuntimeException("Error al obtener generos de series"))
            )
            .bodyToMono(GenreListResponse.class);
    }

    // --- Trending ---

    public Mono<TrendingResponse> getTrending(
        MediaType mediaType,
        TimeWindow timeWindow,
        Integer page
    ) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/trending/{media_type}/{time_window}")
                    .queryParam("page", page)
                    .build(mediaType.name().toLowerCase(), timeWindow.name().toLowerCase())
            )
            .retrieve()
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(new RuntimeException("Error al obtener trending"))
            )
            .bodyToMono(TrendingResponse.class);
    }

    // --- Collections ---

    public Mono<CollectionDetails> getCollection(
        Integer collectionId,
        String language
    ) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/collection/{id}")
                    .queryParam("language", language)
                    .build(collectionId)
            )
            .retrieve()
            .onStatus(
                status -> status.value() == 404,
                response ->
                    Mono.error(
                        new TmdbResourceNotFound(
                            "Coleccion con id " + collectionId + " no encontrada"
                        )
                    )
            )
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(
                    new ErrorFetchingMovieDetails(
                        "Error al obtener coleccion con id " + collectionId
                    )
                )
            )
            .bodyToMono(CollectionDetails.class);
    }

    // --- Search ----

    public Mono<List<SearchResult>> search(
        String query,
        Integer year,
        String mediaType,
        String language
    ) {
        String path = switch (mediaType == null ? "multi" : mediaType) {
            case "movie" -> "/search/movie";
            case "tv" -> "/search/tv";
            case "person" -> "/search/person";
            default -> "/search/multi";
        };

        return webClient
            .get()
            .uri(uriBuilder -> {
                uriBuilder
                    .path(path)
                    .queryParam("query", query)
                    .queryParam("language", language)
                    .queryParam("include_adult", false);

                if (year != null) {
                    if ("tv".equals(mediaType)) {
                        uriBuilder.queryParam("first_air_date_year", year);
                    } else {
                        uriBuilder.queryParam("year", year);
                    }
                }
                return uriBuilder.build();
            })
            .retrieve()
            .onStatus(
                status -> status.value() == 404,
                response ->
                    Mono.error(
                        new TmdbResourceNotFound(
                            "Resultados no encontrados para la busqueda: " +
                                query
                        )
                    )
            )
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(
                    new RuntimeException(
                        "Error al realizar la busqueda en TMDB con la query: " +
                            query
                    )
                )
            )
            .bodyToMono(String.class)
            .map(jsonString -> {
                try {
                    JsonNode rootNode = objectMapper.readTree(jsonString);
                    return mapResults(rootNode, mediaType);
                } catch (Exception e) {
                    throw new RuntimeException(
                        "Error parseando respuesta de TMDB",
                        e
                    );
                }
            });
    }

    private List<SearchResult> mapResults(JsonNode json, String forcedType) {
        List<SearchResult> out = new ArrayList<>();
        JsonNode results = json.path("results");
        if (!results.isArray()) return out;

        for (JsonNode node : results) {
            String type = forcedType != null
                ? forcedType
                : node.path("media_type").asText(null);

            if (type == null) continue;

            String title = node.has("title")
                ? node.path("title").asText(null)
                : node.path("name").asText(null);

            if (title == null) continue;

            String date = node.has("release_date")
                ? node.path("release_date").asText("")
                : node.path("first_air_date").asText("");
            Integer year = extractYear(date);

            String posterPath = "person".equals(type)
                ? emptyToNull(node.path("profile_path").asText(""))
                : emptyToNull(node.path("poster_path").asText(""));

            String knownFor = "person".equals(type)
                ? emptyToNull(node.path("known_for_department").asText(""))
                : type;

            out.add(
                new SearchResult(
                    node.path("id").asLong(),
                    title,
                    emptyToNull(node.path("overview").asText("")),
                    posterPath,
                    type,
                    year,
                    node.path("popularity").asDouble(0),
                    node.path("vote_average").asDouble(0),
                    knownFor
                )
            );
        }
        return out;
    }

    private Integer extractYear(String date) {
        if (date == null || date.length() < 4) return null;
        try {
            return Integer.parseInt(date.substring(0, 4));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
