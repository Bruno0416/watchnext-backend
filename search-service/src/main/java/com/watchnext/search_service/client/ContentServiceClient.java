package com.watchnext.search_service.client;

import com.watchnext.search_service.dto.ContentSearchSection;
import com.watchnext.search_service.dto.SearchType;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ContentServiceClient {

    private final WebClient webClient;
    private final Duration timeout;

    public ContentServiceClient(
        @LoadBalanced WebClient.Builder builder,
        @Value("${search.upstream.content-timeout:5s}") Duration timeout
    ) {
        this.webClient = builder.baseUrl("http://content-service").build();
        this.timeout = timeout;
    }

    // ---------- busqueda de contenido ----------
    public Mono<ContentSearchSection> search(
        String query,
        Set<SearchType> types,
        int page
    ) {
        // 1. construir uri de llamada interna al content service serializando types
        String typesParam = types.stream()
            .map(t -> t.name().toLowerCase(Locale.ROOT))
            .sorted()
            .collect(Collectors.joining(","));

        // 2. emitir peticion reactiva con timeout
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/api/v1/content/internal/search")
                    .queryParam("q", query)
                    .queryParam("types", typesParam)
                    .queryParam("page", page)
                    .build()
            )
            .retrieve()
            .bodyToMono(ContentSearchSection.class)
            .timeout(timeout);
    }
}
