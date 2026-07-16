package com.watchnext.search_service.dto;

import com.watchnext.common.dto.internal.PageResponse;

public record UnifiedSearchResponse(
    ContentSearchSection content,
    PageResponse<UserSummary> users,
    boolean contentAvailable,
    boolean usersAvailable
) {}
