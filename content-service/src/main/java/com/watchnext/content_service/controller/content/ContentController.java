package com.watchnext.content_service.controller.content;

import com.watchnext.common.enums.MediaType;
import com.watchnext.common.enums.TimeWindow;
import com.watchnext.content_service.dto.common.TrendingResponse;
import com.watchnext.content_service.dto.movies.CollectionDetails;
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
@RequestMapping("api/v1/content")
public class ContentController {

    private final ContentService contentService;

    @GetMapping("/trending/{mediaType}/{timeWindow}")
    public Mono<ResponseEntity<TrendingResponse>> getTrending(
        @PathVariable MediaType mediaType,
        @PathVariable TimeWindow timeWindow,
        @RequestParam(defaultValue = "1") Integer page
    ) {
        return contentService
            .getTrending(mediaType, timeWindow, page)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/collections/{id}")
    public Mono<ResponseEntity<CollectionDetails>> getCollection(
        @PathVariable Integer id,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getCollection(id, language)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
