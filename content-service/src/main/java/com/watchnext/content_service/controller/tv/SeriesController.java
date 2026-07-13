package com.watchnext.content_service.controller.tv;

import com.watchnext.content_service.dto.common.GenreListResponse;
import com.watchnext.content_service.dto.common.ReviewResponse;
import com.watchnext.content_service.dto.common.WatchProviders;
import com.watchnext.content_service.dto.tv.TvDetails;
import com.watchnext.content_service.dto.tv.TvEpisode;
import com.watchnext.content_service.dto.tv.TvListResponse;
import com.watchnext.content_service.dto.tv.TvSeasonDetail;
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
@RequestMapping("api/v1/content/tv")
public class SeriesController {

    private final ContentService contentService;

    @GetMapping("/{id}")
    public Mono<ResponseEntity<TvDetails>> getTvDetails(
        @PathVariable Integer id,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getTvDetails(id, language)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/watch-providers")
    public Mono<ResponseEntity<WatchProviders>> getTvWatchProviders(
        @PathVariable Integer id,
        @RequestHeader(value = "X-Region", required = false) String region
    ) {
        return contentService
            .getTvWatchProviders(id, region)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/on-the-air")
    public Mono<ResponseEntity<TvListResponse>> getOnTheAir(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getOnTheAir(page, language)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/popular")
    public Mono<ResponseEntity<TvListResponse>> getPopularTv(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getPopularTv(page, language)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/top-rated")
    public Mono<ResponseEntity<TvListResponse>> getTopRatedTv(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getTopRatedTv(page, language)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}/season/{seasonNumber}")
    public Mono<ResponseEntity<TvSeasonDetail>> getTvSeasonDetail(
        @PathVariable Integer id,
        @PathVariable Integer seasonNumber,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getTvSeasonDetail(id, seasonNumber, language)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/season/{seasonNumber}/episode/{episodeNumber}")
    public Mono<ResponseEntity<TvEpisode>> getTvEpisodeDetail(
        @PathVariable Integer id,
        @PathVariable Integer seasonNumber,
        @PathVariable Integer episodeNumber,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getTvEpisodeDetail(id, seasonNumber, episodeNumber, language)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/discover")
    public Mono<ResponseEntity<TvListResponse>> discoverTv(
        @RequestParam(required = false) String genres,
        @RequestParam(name = "sort_by", defaultValue = "popularity.desc") String sortBy,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "en-US") String language,
        @RequestHeader(value = "X-Region", required = false) String region
    ) {
        return contentService
            .discoverTv(genres, sortBy, page, language, region)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}/reviews")
    public Mono<ResponseEntity<ReviewResponse>> getTvReviews(
        @PathVariable Integer id,
        @RequestParam(defaultValue = "1") Integer page
    ) {
        return contentService
            .getTvReviews(id, page)
            .map(ResponseEntity::ok);
    }


    @GetMapping("/genres")
    public Mono<ResponseEntity<GenreListResponse>> getTvGenres(
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService.getTvGenres(language).map(ResponseEntity::ok);
    }

}
