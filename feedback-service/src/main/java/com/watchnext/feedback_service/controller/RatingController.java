package com.watchnext.feedback_service.controller;

import com.watchnext.common.enums.MediaType;
import com.watchnext.feedback_service.dto.rating.RatingRequest;
import com.watchnext.feedback_service.dto.rating.RatingResponse;
import com.watchnext.feedback_service.service.rating.RatingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/feedback/ratings")
public class RatingController {

    // -------------- Inyección de dependencias --------------
    private final RatingService service;

    // -------------- Endpoints --------------
    // 1. POST /ratings → Crear la calificación del usuario autenticado para un contenido
    @PostMapping("/create")
    public ResponseEntity<Void> createRating(
        @Valid @RequestBody RatingRequest request
    ) {
        service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 2. GET /ratings/me → Obtener todas las calificaciones del usuario autenticado
    @GetMapping("/me")
    public ResponseEntity<List<RatingResponse>> myRatings() {
        return ResponseEntity.ok(service.myRatings());
    }

    // 3. GET /ratings → Obtener la calificación del usuario autenticado para un contenido específico
    @GetMapping
    public ResponseEntity<RatingResponse> getRating(
        @NotNull(message = "tmdbId no puede estar vacío") @Positive(
            message = "tmdbId no puede ser un número negativo"
        ) @RequestParam("tmdbId") Integer tmdbId,
        @NotNull(message = "mediaType no puede estar vacío") @RequestParam(
            "mediaType"
        ) MediaType mediaType
    ) {
        return ResponseEntity.ok(service.getRating(tmdbId, mediaType));
    }

    // 4. DELETE /ratings → Eliminar la calificación del usuario autenticado para un contenido específico
    @DeleteMapping
    public ResponseEntity<Void> deleteRating(
        @NotNull(message = "tmdbId no puede estar vacío") @Positive(
            message = "tmdbId no puede ser un número negativo"
        ) @RequestParam("tmdbId") Integer tmdbId,
        @NotNull(message = "mediaType no puede estar vacío") @RequestParam(
            "mediaType"
        ) MediaType mediaType
    ) {
        service.deleteRating(tmdbId, mediaType);
        return ResponseEntity.noContent().build();
    }
}
