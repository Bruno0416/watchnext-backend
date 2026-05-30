package com.watchnext.content_service.controller;

import com.watchnext.content_service.dto.tv.TvDetails;
import com.watchnext.content_service.dto.tv.TvListResponse;
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
@RequestMapping("api/v1/content/series")
@RequiredArgsConstructor
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
}
