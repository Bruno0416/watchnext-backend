package com.watchnext.content_service.util;

import com.watchnext.content_service.dto.common.CastMember;
import com.watchnext.content_service.dto.common.Video;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class ContentEnrichment {

    private static final int MAX_CAST = 15;

    private ContentEnrichment() {}

    public static List<CastMember> trimCast(List<CastMember> cast) {
        if (cast == null || cast.isEmpty()) return Collections.emptyList();
        return cast
            .stream()
            .sorted(Comparator.comparingInt(CastMember::order))
            .limit(MAX_CAST)
            .toList();
    }

    public static List<Video> normalizeVideos(List<Video> videos) {
        if (videos == null || videos.isEmpty()) return Collections.emptyList();
        return videos
            .stream()
            .filter(v -> "YouTube".equalsIgnoreCase(v.site()))
            .sorted(
                Comparator.comparingInt(
                    ContentEnrichment::typePriority
                ).thenComparingInt(v -> v.official() ? 0 : 1)
            )
            .toList();
    }

    private static int typePriority(Video v) {
        return switch (v.type()) {
            case "Trailer" -> 0;
            case "Teaser" -> 1;
            default -> 2;
        };
    }
}
