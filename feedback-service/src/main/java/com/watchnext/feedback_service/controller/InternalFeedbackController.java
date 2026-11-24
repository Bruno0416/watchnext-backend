package com.watchnext.feedback_service.controller;

import com.watchnext.feedback_service.repository.RatingRepository;
import com.watchnext.feedback_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// rutas internas, solo con token de servicio
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/feedback/internal")
public class InternalFeedbackController {

    private final RatingRepository ratingRepo;
    private final ReviewRepository reviewRepo;

    // ---------- baja de usuario (la llama auth-service desde el panel admin) ----------
    @DeleteMapping("/users/{userId}")
    @Transactional
    public ResponseEntity<Void> deleteUserFeedback(@PathVariable String userId) {
        // 1. borrar sus calificaciones y reseñas
        ratingRepo.deleteAll(ratingRepo.findByUserId(userId));
        reviewRepo.deleteAll(reviewRepo.findAllByUserId(userId));
        return ResponseEntity.noContent().build();
    }
}
