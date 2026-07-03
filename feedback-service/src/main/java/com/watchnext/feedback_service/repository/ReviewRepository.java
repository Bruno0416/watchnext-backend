package com.watchnext.feedback_service.repository;

import com.watchnext.common.enums.MediaType;
import com.watchnext.feedback_service.entity.Review;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    // metodo para validar existencia usando userId + tmdbId + mediaType
    boolean existsByUserIdAndContent_TmdbIdAndContent_MediaType(
        String userId,
        Integer tmdbId,
        MediaType mediaType
    );

    // metodo ver todas las reviews de un contenido
    List<Review> findAllByContent_TmdbIdAndContent_MediaType(
        Integer tmdbId,
        MediaType mediaType
    );

    // metodo para obtener todas las reviews del usuario
    List<Review> findAllByUserId(String userId);

    // metodo para buscar por usuario y reviewId
    Optional<Review> findByIdAndUserId(UUID reviewId, String userId);

    // metodo para eliminar review
    void deleteByIdAndUserId(UUID reviewId, String userId);
}
