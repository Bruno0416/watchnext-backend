package com.watchnext.common.dto;

import com.watchnext.common.model.MediaType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ContentRefResponse {

    Long tmdbId;
    MediaType mediaType;
}
