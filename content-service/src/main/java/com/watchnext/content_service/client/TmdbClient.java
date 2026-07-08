package com.watchnext.content_service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchnext.content_service.dto.movies.MovieDetailsRaw;
import com.watchnext.content_service.dto.movies.MovieListResponse;
import com.watchnext.content_service.dto.search.SearchResult;
import com.watchnext.content_service.dto.tv.TvDetailsRaw;
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

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

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

    // --- Movies ----

    public Mono<MovieDetailsRaw> getMovieDetails(
        Integer movieId,
        String language
    ) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/movie/{id}")
                    .queryParam("append_to_response", "credits,videos")
                    .queryParam("language", language)
                    .build(movieId)
            )
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
        return fetchMovieList("/movie/now_playing", page, language);
    }

    public Mono<MovieListResponse> getPopular(Integer page, String language) {
        return fetchMovieList("/movie/popular", page, language);
    }

    public Mono<MovieListResponse> getTopRated(Integer page, String language) {
        return fetchMovieList("/movie/top_rated", page, language);
    }

    public Mono<MovieListResponse> getUpcoming(Integer page, String language) {
        return fetchMovieList("/movie/upcoming", page, language);
    }

    private Mono<MovieListResponse> fetchMovieList(
        String path,
        Integer page,
        String language
    ) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(path)
                    .queryParam("language", language)
                    .queryParam("page", page)
                    .build()
            )
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

    // --- TV Series ----

    public Mono<TvDetailsRaw> getTvDetails(Integer tvId, String language) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/tv/{id}")
                    .queryParam("append_to_response", "credits,videos")
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

    public Mono<TvListResponse> getOnTheAirTv(Integer page, String language) {
        return fetchTvList("/tv/on_the_air", page, language);
    }

    public Mono<TvListResponse> getPopularTv(Integer page, String language) {
        return fetchTvList("/tv/popular", page, language);
    }

    public Mono<TvListResponse> getTopRatedTv(Integer page, String language) {
        return fetchTvList("/tv/top_rated", page, language);
    }

    private Mono<TvListResponse> fetchTvList(
        String path,
        Integer page,
        String language
    ) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path(path)
                    .queryParam("language", language)
                    .queryParam("page", page)
                    .build()
            )
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
                            "Resultados no encontrados para la búsqueda: " +
                                query
                        )
                    )
            )
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(
                    new RuntimeException(
                        "Error al realizar la búsqueda en TMDB con la query: " +
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
            String type =
                forcedType != null
                    ? forcedType
                    : node.path("media_type").asText(null);

            if (type == null || type.equals("person")) continue;

            String title = node.has("title")
                ? node.path("title").asText(null)
                : node.path("name").asText(null);

            if (title == null) continue;

            String date = node.has("release_date")
                ? node.path("release_date").asText("")
                : node.path("first_air_date").asText("");
            Integer year = extractYear(date);

            out.add(
                new SearchResult(
                    node.path("id").asLong(),
                    title,
                    emptyToNull(node.path("overview").asText("")),
                    emptyToNull(node.path("poster_path").asText("")),
                    type,
                    year,
                    node.path("popularity").asDouble(0),
                    node.path("vote_average").asDouble(0)
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
