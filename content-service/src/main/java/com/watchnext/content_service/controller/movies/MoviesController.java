package com.watchnext.content_service.controller.movies;

import com.watchnext.common.security.GatewayHeaders;
import com.watchnext.content_service.dto.common.GenreListResponse;
import com.watchnext.content_service.dto.common.ReviewResponse;
import com.watchnext.content_service.dto.common.WatchProvider;
import com.watchnext.content_service.dto.movies.MovieDetails;
import com.watchnext.content_service.dto.movies.MovieListResponse;
import com.watchnext.content_service.service.content.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/content/movies")
@Validated
public class MoviesController {

    private final ContentService contentService;

    // ---------- detalles y metadatos ----------
    @GetMapping("/{id}")
    public Mono<ResponseEntity<MovieDetails>> getMovieDetails(
        @PathVariable @Positive(message = "El id debe ser un número positivo") Integer id,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentService
            .getMovieDetails(id, language)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/watch-providers")
    public Mono<ResponseEntity<List<WatchProvider>>> getMovieWatchProviders(
        @PathVariable @Positive(message = "El id debe ser un número positivo") Integer id,
@RequestHeader(GatewayHeaders.COUNTRY) String country,
@RequestHeader(GatewayHeaders.REGION) String region
    ) {
        return contentService
            .getMovieWatchProviders(id, country, region)
            .map(ResponseEntity::ok);
    }

    // ---------- listados de peliculas ----------
    @GetMapping("/now-playing")
    public Mono<ResponseEntity<MovieListResponse>> getNowPlayingMovies(
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "La página mínima es 1") @Max(value = 500, message = "La página máxima es 500") Integer page,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentService
            .getNowPlayingMovies(page, language)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/popular")
    public Mono<ResponseEntity<MovieListResponse>> getPopularMovies(
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "La página mínima es 1") @Max(value = 500, message = "La página máxima es 500") Integer page,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentService
            .getPopularMovies(page, language)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/top-rated")
    public Mono<ResponseEntity<MovieListResponse>> getTopRatedMovies(
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "La página mínima es 1") @Max(value = 500, message = "La página máxima es 500") Integer page,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentService
            .getTopRatedMovies(page, language)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/upcoming")
    public Mono<ResponseEntity<MovieListResponse>> getUpcomingMovies(
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "La página mínima es 1") @Max(value = 500, message = "La página máxima es 500") Integer page,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentService
            .getUpcomingMovies(page, language)
            .map(ResponseEntity::ok);
    }

    // ---------- busqueda y filtrado ----------
    @GetMapping("/discover")
    public Mono<ResponseEntity<MovieListResponse>> discoverMovies(
        @RequestParam(required = false) @Pattern(regexp = "^\\d+(,\\d+)*$", message = "Los géneros deben ser una lista de IDs separados por comas") String genres,
        @RequestParam(name = "sort_by", defaultValue = "popularity.desc")
        @Pattern(
            regexp = "^(popularity|release_date|revenue|primary_release_date|original_title|vote_average|vote_count)\\.(asc|desc)$",
            message = "sort_by debe coincidir con las opciones de ordenamiento permitidas por TMDB para películas"
        ) String sortBy,
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "La página mínima es 1") @Max(value = 500, message = "La página máxima es 500") Integer page,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language,
        @RequestHeader(value = "Region", required = false) @Size(max = 20, message = "La región no puede exceder los 20 caracteres") String region
    ) {
        return contentService
            .discoverMovies(genres, sortBy, page, language, region)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/by-genre")
    public Mono<ResponseEntity<MovieListResponse>> getMoviesByGenre(
        @RequestParam @NotEmpty(message = "El conjunto de géneros no puede estar vacío") Set<Integer> genre,
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "La página mínima es 1") @Max(value = 500, message = "La página máxima es 500") Integer page,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language,
        @RequestHeader(value = "Region", required = false) @Size(max = 20, message = "La región no puede exceder los 20 caracteres") String region
    ) {
        return contentService
            .getMoviesByGenre(genre, page, language, region)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}/reviews")
    public Mono<ResponseEntity<ReviewResponse>> getMovieReviews(
        @PathVariable @Positive(message = "El id debe ser un número positivo") Integer id,
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "La página mínima es 1") @Max(value = 500, message = "La página máxima es 500") Integer page
    ) {
        return contentService
            .getMovieReviews(id, page)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/genres")
    public Mono<ResponseEntity<GenreListResponse>> getMovieGenres(
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentService.getMovieGenres(language).map(ResponseEntity::ok);
    }
}
