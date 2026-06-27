package com.watchnext.feedback_service.mapper;

import com.watchnext.feedback_service.dto.rating.RatingRequest;
import com.watchnext.feedback_service.dto.rating.RatingResponse;
import com.watchnext.feedback_service.entity.Rating;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RatingMapper {
    // dto -> entidad
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "request.tmdbId", target = "content.tmdbId")
    @Mapping(source = "request.mediaType", target = "content.mediaType")
    @Mapping(source = "request.score", target = "score")
    Rating toEntity(RatingRequest request, String userId);

    // entidad -> dto
    @Mapping(source = "content.tmdbId", target = "tmdbId")
    @Mapping(source = "content.mediaType", target = "mediaType")
    RatingResponse toResponse(Rating rating);

    // lista de entidad -> lista dto
    List<RatingResponse> toResponseList(List<Rating> ratings);
}
