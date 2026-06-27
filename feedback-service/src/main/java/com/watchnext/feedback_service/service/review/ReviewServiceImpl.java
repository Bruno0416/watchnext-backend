package com.watchnext.feedback_service.service.review;

import com.watchnext.common.model.MediaType;
import com.watchnext.common.model.User;
import com.watchnext.common.security.CurrentUser;
import com.watchnext.feedback_service.dto.review.ReviewRequest;
import com.watchnext.feedback_service.dto.review.ReviewResponse;
import com.watchnext.feedback_service.dto.review.ReviewUpdateRequest;
import com.watchnext.feedback_service.entity.Review;
import com.watchnext.feedback_service.exceptions.ReviewAlreadyExists;
import com.watchnext.feedback_service.exceptions.ReviewNotFound;
import com.watchnext.feedback_service.mapper.ReviewMapper;
import com.watchnext.feedback_service.repository.ReviewRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repo;
    private final ReviewMapper mapper;

    @Override
    public void createReview(ReviewRequest request) {
        // 1. obtener usuario
        User user = CurrentUser.get();
        // 2. validar que no exista previamente
        if (
            repo.existsByUserIdAndContent_TmdbIdAndContent_MediaType(
                user.id(),
                request.tmdbId(),
                request.mediaType()
            )
        ) {
            throw new ReviewAlreadyExists();
        }

        // 3. crear y guardar
        Review review = mapper.toEntity(request, user.id());
        repo.save(review);
    }

    @Override
    public List<ReviewResponse> getAllContentReviews(
        Integer tmdbId,
        MediaType mediaType
    ) {
        // ver todas las reviews de un contenido (sin validacion de usuario)
        return repo
            .findAllByContent_TmdbIdAndContent_MediaType(tmdbId, mediaType)
            .stream()
            .map(mapper::toResponse)
            .toList();
    }

    @Override
    public List<ReviewResponse> getMyReviews() {
        // 1. obtener usuario
        User user = CurrentUser.get();

        // 2. buscar reviews del usuario y retornar
        return repo
            .findAllByUserId(user.id())
            .stream()
            .map(mapper::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public void updateReview(UUID reviewId, ReviewUpdateRequest request) {
        // 1. obtener usuario
        User user = CurrentUser.get();

        // 2. obtener review
        Review review = repo
            .findByIdAndUserId(reviewId, user.id())
            .orElseThrow(ReviewNotFound::new);

        // 3. actualizar review
        mapper.updateEntity(review, request);
    }

    @Override
    @Transactional
    public void deleteReview(UUID reviewId) {
        // 1. obtener usuario
        User user = CurrentUser.get();

        // 2. obtener review
        Review review = repo
            .findByIdAndUserId(reviewId, user.id())
            .orElseThrow(ReviewNotFound::new);

        // 3. eliminar review
        repo.delete(review);
    }
}
