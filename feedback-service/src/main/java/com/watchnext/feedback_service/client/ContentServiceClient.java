package com.watchnext.feedback_service.client;

import com.watchnext.feedback_service.dto.aggregated.TmdbReviewPage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ContentServiceClient {

    private final RestClient restClient;

    public ContentServiceClient(
        @Value("${content-service.api.base-url}") String baseUrl
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    // ---------- Reviews de TMDB ----------

    public TmdbReviewPage getMovieReviews(Integer tmdbId, int page) {
        // 1. obtener reviews de pelicula desde content-service
        return restClient
            .get()
            .uri("/movies/{id}/reviews?page={page}", tmdbId, page)
            .retrieve()
            .body(TmdbReviewPage.class);
    }

    public TmdbReviewPage getTvReviews(Integer tmdbId, int page) {
        // 1. obtener reviews de serie desde content-service
        return restClient
            .get()
            .uri("/tv/{id}/reviews?page={page}", tmdbId, page)
            .retrieve()
            .body(TmdbReviewPage.class);
    }
}