package com.watchnext.feedback_service.repository;

import com.watchnext.common.model.MediaType;
import com.watchnext.feedback_service.entity.Rating;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, UUID> {
    Optional<Rating> findByUserIdAndContent_TmdbIdAndContent_MediaType(
        String userId,
        Integer tmdbId,
        MediaType mediaType
    );

    // metodo para validar existencia usando userId + tmdbId + mediaType
    boolean existsByUserIdAndContent_TmdbIdAndContent_MediaType(
        String userId,
        Integer tmdbId,
        MediaType mediaType
    );

    // buscar todas las listas del usuario
    List<Rating> findByUserId(String userId);
}
