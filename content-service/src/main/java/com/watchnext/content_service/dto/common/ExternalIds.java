package com.watchnext.content_service.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalIds(
    @JsonProperty("imdb_id") String imdbId,
    @JsonProperty("facebook_id") String facebookId,
    @JsonProperty("instagram_id") String instagramId,
    @JsonProperty("twitter_id") String twitterId
) {}
