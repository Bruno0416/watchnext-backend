package com.watchnext.news_service.service;

import com.watchnext.news_service.dto.NewsRequest;
import com.watchnext.news_service.dto.NewsResponse;
import java.util.List;
import java.util.UUID;

public interface NewsService {
    // ──────────────────── Consultas publicas ────────────────────
    List<NewsResponse> getAll();

    NewsResponse getById(UUID id);

    NewsResponse getBySlug(String slug);

    // ──────────────────── Administracion ────────────────────
    NewsResponse create(NewsRequest request);

    NewsResponse update(UUID id, NewsRequest request);

    void delete(UUID id);
}
