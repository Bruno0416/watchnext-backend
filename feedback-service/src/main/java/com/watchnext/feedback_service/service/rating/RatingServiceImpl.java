package com.watchnext.feedback_service.service.rating;

import com.watchnext.common.model.MediaType;
import com.watchnext.common.model.User;
import com.watchnext.common.security.CurrentUser;
import com.watchnext.feedback_service.dto.rating.RatingRequest;
import com.watchnext.feedback_service.dto.rating.RatingResponse;
import com.watchnext.feedback_service.entity.Rating;
import com.watchnext.feedback_service.exceptions.RatingAlreadyExists;
import com.watchnext.feedback_service.exceptions.RatingNotFound;
import com.watchnext.feedback_service.mapper.RatingMapper;
import com.watchnext.feedback_service.repository.RatingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository repo;
    private final RatingMapper mapper;

    @Override
    public void create(RatingRequest request) {
        // 1. obtener usuario
        User user = CurrentUser.get();

        // 2. validar que no exista rating para el recurso
        if (
            repo.existsByUserIdAndContent_TmdbIdAndContent_MediaType(
                user.id(),
                request.tmdbId(),
                request.mediaType()
            )
        ) {
            throw new RatingAlreadyExists();
        }

        // 3. crear rating
        Rating rating = mapper.toEntity(request, user.id());
        repo.save(rating);
    }

    @Override
    public List<RatingResponse> myRatings() {
        // 1. obtener usuario
        User user = CurrentUser.get();

        // 2. buscar y devolver listas del usuario
        List<Rating> ratings = repo.findByUserId(user.id());
        return mapper.toResponseList(ratings);
    }

    @Override
    public RatingResponse getRating(Integer tmdbId, MediaType mediaType) {
        // 1. obtener usuario
        User user = CurrentUser.get();

        // 2. buscar y validar rating
        Rating rating = repo
            .findByUserIdAndContent_TmdbIdAndContent_MediaType(
                user.id(),
                tmdbId,
                mediaType
            )
            .orElseThrow(RatingNotFound::new);

        // 3. devolver rating
        return mapper.toResponse(rating);
    }

    @Override
    public void deleteRating(Integer tmdbId, MediaType mediaType) {
        // 1. obtener usuario
        User user = CurrentUser.get();

        // 2. buscar y validar rating
        Rating rating = repo
            .findByUserIdAndContent_TmdbIdAndContent_MediaType(
                user.id(),
                tmdbId,
                mediaType
            )
            .orElseThrow(RatingNotFound::new);

        // 3. eliminar rating
        repo.delete(rating);
    }
}
