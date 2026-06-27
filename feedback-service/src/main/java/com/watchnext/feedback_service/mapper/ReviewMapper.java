package com.watchnext.feedback_service.mapper;

import com.watchnext.feedback_service.dto.review.ReviewRequest;
import com.watchnext.feedback_service.dto.review.ReviewResponse;
import com.watchnext.feedback_service.dto.review.ReviewUpdateRequest;
import com.watchnext.feedback_service.entity.Review;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    // 1. dto -> entidad (crear)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "request.tmdbId", target = "content.tmdbId")
    @Mapping(source = "request.mediaType", target = "content.mediaType")
    Review toEntity(ReviewRequest request, String userId);

    // 2. entidad -> dto
    @Mapping(source = "content.tmdbId", target = "tmdbId")
    @Mapping(source = "content.mediaType", target = "mediaType")
    ReviewResponse toResponse(Review review);

    // 3. entidad -> dto (Lista)
    List<ReviewResponse> toResponseList(List<Review> reviews);

    // 4. Actualizar Entidad existente (desde UpdateRequest)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(
        @MappingTarget Review review,
        ReviewUpdateRequest request
    );
}
