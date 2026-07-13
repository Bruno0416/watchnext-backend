package com.watchnext.content_service.controller.search;

import com.watchnext.content_service.dto.search.SearchResponse;
import com.watchnext.content_service.service.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/content/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public Mono<SearchResponse> search(
        @RequestParam("q") String query,
        @RequestParam(
            value = "language",
            defaultValue = "en-US"
        ) String language
    ) {
        return searchService.search(query, language);
    }
}
