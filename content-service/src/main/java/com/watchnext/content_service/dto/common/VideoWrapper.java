package com.watchnext.content_service.dto.common;

import java.util.List;

/**
 * Internal wrapper that models TMDB's videos object: { "results": [...] }.
 * Used only for deserializing the raw TMDB response; not exposed to API clients.
 */
public record VideoWrapper(List<Video> results) {}
