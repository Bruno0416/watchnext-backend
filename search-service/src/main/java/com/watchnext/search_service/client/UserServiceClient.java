package com.watchnext.search_service.client;

import com.watchnext.common.dto.internal.PageResponse;
import com.watchnext.search_service.dto.UserSummary;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {

    private final WebClient webClient;
    private final Duration timeout;

    public UserServiceClient(
        @LoadBalanced WebClient.Builder builder,
        @Value("${search.upstream.user-timeout:5s}") Duration timeout
    ) {
        this.webClient = builder.baseUrl("http://user-service").build();
        this.timeout = timeout;
    }

    // ---------- busqueda de usuarios ----------
    public Mono<PageResponse<UserSummary>> search(String query, int page) {
        // 1. emitir peticion reactiva al endpoint interno de usuarios con timeout
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/api/v1/users/internal/search")
                    .queryParam("q", query)
                    .queryParam("page", page)
                    .build()
            )
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<PageResponse<UserSummary>>() {})
            .timeout(timeout);
    }
}
