package com.watchnext.content_service.service.content;

import com.watchnext.content_service.dto.movies.MovieDetails;
import com.watchnext.content_service.dto.movies.MovieListResponse;
import com.watchnext.content_service.dto.tv.TvDetails;
import com.watchnext.content_service.dto.tv.TvListResponse;
import com.watchnext.content_service.dto.tv.TvSeasonDetail;
import reactor.core.publisher.Mono;

public interface ContentService {
    // ------> Movies <------
    // 1. Obtener detalle pelicula (con cast y videos)
    Mono<MovieDetails> getMovieDetails(Integer movieId, String language);
    // 2. Now Playing
    Mono<MovieListResponse> getNowPlayingMovies(Integer page, String language);
    // 3. Popular
    Mono<MovieListResponse> getPopularMovies(Integer page, String language);
    // 4. Top Rated
    Mono<MovieListResponse> getTopRatedMovies(Integer page, String language);
    // 5. Upcoming
    Mono<MovieListResponse> getUpcomingMovies(Integer page, String language);

    // ------> TV Series <------
    // 1. Obtener detalle Serie (con cast y videos; seasons sin episodios)
    Mono<TvDetails> getTvDetails(Integer tvId, String language);
    // 2. On The Air
    Mono<TvListResponse> getOnTheAir(Integer page, String language);
    // 3. Popular
    Mono<TvListResponse> getPopularTv(Integer page, String language);
    // 4. Top Rated
    Mono<TvListResponse> getTopRatedTv(Integer page, String language);
    // 5. Obtener episodios de una temporada especifica
    Mono<TvSeasonDetail> getTvSeasonDetail(
        Integer tvId,
        Integer seasonNumber,
        String language
    );
}
