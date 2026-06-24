package com.watchnext.content_service.controller.tv;

import com.watchnext.content_service.dto.tv.TvDetails;
import com.watchnext.content_service.dto.tv.TvListResponse;
import com.watchnext.content_service.service.content.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/content/tv")
public class SeriesController {

    private final ContentService contentService;

    // 1. obtener detalles de serie especifica
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

    // 2. obtener lista de series en emision
    @GetMapping("/on-the-air")
    public Mono<ResponseEntity<TvListResponse>> getOnTheAir(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getOnTheAir(page, language)
            .map(ResponseEntity::ok);
    }

    // 3. obtener lista de series populares
    @GetMapping("/popular")
    public Mono<ResponseEntity<TvListResponse>> getPopularTv(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getPopularTv(page, language)
            .map(ResponseEntity::ok);
    }

    // 4. obtener lista de series mejor valoradas
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
