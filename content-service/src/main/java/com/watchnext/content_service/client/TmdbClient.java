package com.watchnext.content_service.client;

import com.watchnext.content_service.dto.movies.MovieDetails;
import com.watchnext.content_service.dto.movies.MovieListResponse;
import com.watchnext.content_service.dto.tv.TvDetails;
import com.watchnext.content_service.dto.tv.TvListResponse;
import com.watchnext.content_service.dto.tv.TvSeason;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Component
public class TmdbClient {

    private final WebClient webClient;

    public TmdbClient(
        @Value("${tmdb.api.base-url}") String baseUrl,
        @Value("${tmdb.api.key}") String apiKey
    ) {
        HttpClient httpClient = HttpClient.create().responseTimeout(
            Duration.ofSeconds(10)
        );

        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();
    }

    // -----> Movies <-----

    public Mono<MovieDetails> getMovieDetails(
        Integer movieId,
        String language
    ) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/movie/{id}")
                    .queryParam("language", language)
                    .build(movieId)
            )
            .retrieve()
            .bodyToMono(MovieDetails.class);
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
            .bodyToMono(MovieListResponse.class);
    }

    // -----> TV Series <-----

    public Mono<TvDetails> getTvDetails(Integer tvId, String language) {
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/tv/{id}")
                    .queryParam("language", language)
                    .build(tvId)
            )
            .retrieve()
            .bodyToMono(TvDetails.class);
    }

    public Mono<TvSeason> getTvSeason(
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
            .bodyToMono(TvSeason.class);
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
            .bodyToMono(TvListResponse.class);
    }
}
