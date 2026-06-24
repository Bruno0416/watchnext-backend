package com.watchnext.content_service.service.search;

import com.watchnext.content_service.dto.search.SearchResponse;
import reactor.core.publisher.Mono;

public interface SearchService {
    Mono<SearchResponse> search(String rawQuery, String language);
}
