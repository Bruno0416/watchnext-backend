package com.watchnext.content_service.controller;

import com.watchnext.content_service.dto.movies.MovieDetails;
import com.watchnext.content_service.dto.movies.MovieListResponse;
import com.watchnext.content_service.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/content/movies")
@RequiredArgsConstructor
public class MoviesController {

    private final ContentService contentService;

    // 1. Detalle de Película
    @GetMapping("/{id}")
    public Mono<ResponseEntity<MovieDetails>> getMovieDetails(
        @PathVariable Integer id,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getMovieDetails(id, language)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // 2. Now Playing
    @GetMapping("/now-playing")
    public Mono<ResponseEntity<MovieListResponse>> getNowPlayingMovies(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getNowPlayingMovies(page, language)
            .map(ResponseEntity::ok);
    }

    // 3. Popular
    @GetMapping("/popular")
    public Mono<ResponseEntity<MovieListResponse>> getPopularMovies(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getPopularMovies(page, language)
            .map(ResponseEntity::ok);
    }

    // 4. Top Rated
    @GetMapping("/top-rated")
    public Mono<ResponseEntity<MovieListResponse>> getTopRatedMovies(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getTopRatedMovies(page, language)
            .map(ResponseEntity::ok);
    }

    // 5. Upcoming
    @GetMapping("/upcoming")
    public Mono<ResponseEntity<MovieListResponse>> getUpcomingMovies(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getUpcomingMovies(page, language)
            .map(ResponseEntity::ok);
    }
}
