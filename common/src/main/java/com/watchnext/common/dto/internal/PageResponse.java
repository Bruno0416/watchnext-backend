package com.watchnext.common.dto.internal;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public static <T> PageResponse<T> of(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
    ) {
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }
}
