package com.watchnext.content_service.controller.movies;

import com.watchnext.content_service.dto.common.GenreListResponse;
import com.watchnext.content_service.dto.common.ReviewResponse;
import com.watchnext.content_service.dto.common.WatchProviders;
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
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/content/movies")
public class MoviesController {

    private final ContentService contentService;

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

    @GetMapping("/{id}/watch-providers")
    public Mono<ResponseEntity<WatchProviders>> getMovieWatchProviders(
        @PathVariable Integer id,
        @RequestHeader(value = "X-Region", required = false) String region
    ) {
        return contentService
            .getMovieWatchProviders(id, region)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/now-playing")
    public Mono<ResponseEntity<MovieListResponse>> getNowPlayingMovies(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getNowPlayingMovies(page, language)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/popular")
    public Mono<ResponseEntity<MovieListResponse>> getPopularMovies(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getPopularMovies(page, language)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/top-rated")
    public Mono<ResponseEntity<MovieListResponse>> getTopRatedMovies(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getTopRatedMovies(page, language)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/upcoming")
    public Mono<ResponseEntity<MovieListResponse>> getUpcomingMovies(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getUpcomingMovies(page, language)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/discover")
    public Mono<ResponseEntity<MovieListResponse>> discoverMovies(
        @RequestParam(required = false) String genres,
        @RequestParam(name = "sort_by", defaultValue = "popularity.desc") String sortBy,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "en-US") String language,
        @RequestHeader(value = "X-Region", required = false) String region
    ) {
        return contentService
            .discoverMovies(genres, sortBy, page, language, region)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}/reviews")
    public Mono<ResponseEntity<ReviewResponse>> getMovieReviews(
        @PathVariable Integer id,
        @RequestParam(defaultValue = "1") Integer page
    ) {
        return contentService
            .getMovieReviews(id, page)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/genres")
    public Mono<ResponseEntity<GenreListResponse>> getMovieGenres(
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService.getMovieGenres(language).map(ResponseEntity::ok);
    }
}
