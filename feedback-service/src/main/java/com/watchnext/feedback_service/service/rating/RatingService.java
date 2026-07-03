package com.watchnext.feedback_service.service.rating;

import com.watchnext.common.enums.MediaType;
import com.watchnext.feedback_service.dto.rating.RatingRequest;
import com.watchnext.feedback_service.dto.rating.RatingResponse;
import java.util.List;

public interface RatingService {
    // -------------- Ratings --------------
    // 1. POST /ratings → Crear o actualizar la calificación del usuario autenticado para un contenido
    void create(RatingRequest request);
    // 2. GET /ratings/me → Obtener todas las calificaciones del usuario autenticado
    List<RatingResponse> myRatings();
    // 3. GET /ratings → Obtener la calificación del usuario autenticado para un contenido específico (pathVariable: tmdbId | param: mediaType)
    RatingResponse getRating(Integer tmdbId, MediaType mediaType);
    // 4. DELETE /ratings → Eliminar la calificación del usuario autenticado para un contenido específico (pathVariable: tmdbId | param: mediaType)
    void deleteRating(Integer tmdbId, MediaType mediaType);
}
