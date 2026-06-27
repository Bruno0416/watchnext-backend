package com.watchnext.content_service.dto.common;

import java.util.List;

/**
 * Internal wrapper that models TMDB's credits object: { "cast": [...] }.
 * Used only for deserializing the raw TMDB response; not exposed to API clients.
 */
public record Credits(List<CastMember> cast) {}
