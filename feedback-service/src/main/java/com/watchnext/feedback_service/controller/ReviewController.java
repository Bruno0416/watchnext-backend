package com.watchnext.feedback_service.controller;

import com.watchnext.common.enums.MediaType;
import com.watchnext.feedback_service.dto.review.ReviewRequest;
import com.watchnext.feedback_service.dto.review.ReviewResponse;
import com.watchnext.feedback_service.dto.review.ReviewUpdateRequest;
import com.watchnext.feedback_service.service.review.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/feedback/reviews")
public class ReviewController {

    // -------------- Inyección de dependencias --------------
    private final ReviewService service;

    // -------------- ENDPOINTS --------------
    // 1. POST /reviews → Crear una reseña para un contenido
    @PostMapping("/create")
    public ResponseEntity<Void> createReview(
        @Valid @RequestBody ReviewRequest request
    ) {
        service.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 2. GET /reviews → Obtener las reseñas públicas de un contenido específico (param: tmdbId | param: mediaType) (sin auth)
    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getAllContentReviews(
        @NotNull(message = "tmdbId no puede estar vacío") @Positive(
            message = "tmdbId no puede ser un número negativo"
        ) @RequestParam("tmdbId") Integer tmdbId,
        @NotNull(message = "mediaType no puede estar vacío") @RequestParam(
            "mediaType"
        ) MediaType mediaType
    ) {
        return ResponseEntity.ok(
            service.getAllContentReviews(tmdbId, mediaType)
        );
    }

    // 3. GET /reviews/me → Obtener todas las reseñas del usuario autenticado (TODO: mezclar endpoint con content-service)
    @GetMapping("/me")
    public ResponseEntity<List<ReviewResponse>> getAllUserReviews() {
        return ResponseEntity.ok(service.getMyReviews());
    }

    // 4. PUT /reviews/update/{id} → Editar una reseña propia por ID
    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateReview(
        @NotNull(message = "El id no puede estar vacío") @PathVariable UUID id,
        @Valid @RequestBody ReviewUpdateRequest request
    ) {
        service.updateReview(id, request);
        return ResponseEntity.ok().build();
    }

    // 5. DELETE /reviews/{id} → Eliminar una reseña propia por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
        @NotNull(message = "El id no puede estar vacío") @PathVariable UUID id
    ) {
        service.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
