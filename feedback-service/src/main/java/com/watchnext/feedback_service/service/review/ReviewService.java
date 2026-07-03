package com.watchnext.feedback_service.service.review;

import com.watchnext.common.enums.MediaType;
import com.watchnext.feedback_service.dto.review.ReviewRequest;
import com.watchnext.feedback_service.dto.review.ReviewResponse;
import com.watchnext.feedback_service.dto.review.ReviewUpdateRequest;
import java.util.List;
import java.util.UUID;

public interface ReviewService {
    // 1. POST /reviews → Crear una reseña para un contenido
    void createReview(ReviewRequest request);
    // 2. GET /reviews → Obtener las reseñas públicas de un contenido específico
    List<ReviewResponse> getAllContentReviews(
        Integer tmdbId,
        MediaType mediaType
    );
    // 3. GET /reviews/me → Obtener todas las reseñas del usuario autenticado
    List<ReviewResponse> getMyReviews();
    // 4. PUT /reviews/{id} → Editar una reseña propia por ID
    void updateReview(UUID reviewId, ReviewUpdateRequest request);
    // 5. DELETE /reviews/{id} → Eliminar una reseña propia por ID
    void deleteReview(UUID reviewId);
}
