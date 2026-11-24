package com.watchnext.news_service.dto;

import java.time.Instant;
import java.util.UUID;

// mismo formato que el mock del front (core/mock/news)
public record NewsResponse(
    UUID id,
    String slug,
    String title,
    String summary,
    String content,
    String imageUrl,
    Instant publishedAt
) {}
