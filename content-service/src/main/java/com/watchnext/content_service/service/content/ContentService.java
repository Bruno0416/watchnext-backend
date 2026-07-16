package com.watchnext.content_service.service.content;

import com.watchnext.common.enums.MediaType;
import com.watchnext.common.enums.TimeWindow;
import com.watchnext.content_service.dto.common.GenreListResponse;
import com.watchnext.content_service.dto.common.ReviewResponse;
import com.watchnext.content_service.dto.common.TrendingResponse;
import com.watchnext.content_service.dto.common.WatchProvider;
import com.watchnext.content_service.dto.movies.CollectionDetails;
import com.watchnext.content_service.dto.movies.MovieDetails;
import com.watchnext.content_service.dto.movies.MovieListResponse;
import com.watchnext.content_service.dto.persons.PersonDetails;
import com.watchnext.content_service.dto.tv.TvDetails;
import com.watchnext.content_service.dto.tv.TvEpisode;
import com.watchnext.content_service.dto.tv.TvListResponse;
import com.watchnext.content_service.dto.tv.TvSeasonDetail;
import java.util.List;
import reactor.core.publisher.Mono;

public interface ContentService {
    // ------> Movies <------
    Mono<MovieDetails> getMovieDetails(Integer movieId, String language);
    Mono<List<WatchProvider>> getMovieWatchProviders(Integer movieId, String country, String region);
    Mono<MovieListResponse> getNowPlayingMovies(Integer page, String language);
    Mono<MovieListResponse> getPopularMovies(Integer page, String language);
    Mono<MovieListResponse> getTopRatedMovies(Integer page, String language);
    Mono<MovieListResponse> getUpcomingMovies(Integer page, String language);
    Mono<MovieListResponse> discoverMovies(String genres, String sortBy, Integer page, String language, String region);
    Mono<MovieListResponse> getMoviesByGenre(java.util.Set<Integer> genre, Integer page, String language, String region);
    Mono<ReviewResponse> getMovieReviews(Integer movieId, Integer page);

    // ------> TV Series <------
    Mono<TvDetails> getTvDetails(Integer tvId, String language);
    Mono<List<WatchProvider>> getTvWatchProviders(Integer tvId, String country, String region);
    Mono<TvListResponse> getOnTheAir(Integer page, String language);
    Mono<TvListResponse> getPopularTv(Integer page, String language);
    Mono<TvListResponse> getTopRatedTv(Integer page, String language);
    Mono<TvSeasonDetail> getTvSeasonDetail(Integer tvId, Integer seasonNumber, String language);
    Mono<TvEpisode> getTvEpisodeDetail(Integer tvId, Integer seasonNumber, Integer episodeNumber, String language);
    Mono<TvListResponse> discoverTv(String genres, String sortBy, Integer page, String language, String region);
    Mono<TvListResponse> getSeriesByGenre(java.util.Set<Integer> genre, Integer page, String language, String region);
    Mono<ReviewResponse> getTvReviews(Integer tvId, Integer page);

    // ------> Persons <------
    Mono<PersonDetails> getPersonDetails(Long personId, String language);

    // ------> Genres <------
    Mono<GenreListResponse> getMovieGenres(String language);
    Mono<GenreListResponse> getTvGenres(String language);

    // ------> Trending <------
    Mono<TrendingResponse> getTrending(MediaType mediaType, TimeWindow timeWindow, Integer page);

    // ------> Collections <------
    Mono<CollectionDetails> getCollection(Integer collectionId, String language);
}
