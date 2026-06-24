package com.watchnext.content_service.dto.search;

public record ParsedQuery(String cleanQuery, Integer year, String mediaType) {
    public boolean hasYear() {
        return year != null;
    }

    public int wordCount() {
        if (cleanQuery == null || cleanQuery.isBlank()) return 0;
        return cleanQuery.trim().split("\\s+").length;
    }
}
