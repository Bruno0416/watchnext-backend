package com.watchnext.feedback_service.service.review;

import com.watchnext.common.enums.MediaType;
import com.watchnext.feedback_service.client.ContentServiceClient;
import com.watchnext.feedback_service.dto.aggregated.AggregatedReviewsResponse;
import com.watchnext.feedback_service.dto.aggregated.ReviewSource;
import com.watchnext.feedback_service.dto.aggregated.TmdbReviewItem;
import com.watchnext.feedback_service.dto.aggregated.TmdbReviewPage;
import com.watchnext.feedback_service.dto.aggregated.TmdbReviewsSection;
import com.watchnext.feedback_service.dto.aggregated.WatchNextReviewItem;
import com.watchnext.feedback_service.dto.aggregated.WatchNextReviewsSection;
import com.watchnext.feedback_service.entity.Review;
import com.watchnext.feedback_service.repository.ReviewRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AggregatedReviewServiceImpl implements AggregatedReviewService {

    private final ReviewRepository reviewRepository;
    private final ContentServiceClient contentClient;

    @Override
    public AggregatedReviewsResponse getAggregatedReviews(
        MediaType mediaType,
        Integer tmdbId,
        int page,
        int size
    ) {
        // 1. obtener reviews propias de watchnext con paginacion
        WatchNextReviewsSection watchnext = buildWatchNextSection(
            mediaType, tmdbId, page, size
        );

        // 2. obtener reviews de tmdb con degradacion gracil
        TmdbReviewsSection tmdb = buildTmdbSection(mediaType, tmdbId, page);

        return new AggregatedReviewsResponse(watchnext, tmdb);
    }

    // --- Secciones privadas ---

    private WatchNextReviewsSection buildWatchNextSection(
        MediaType mediaType,
        Integer tmdbId,
        int page,
        int size
    ) {
        // 1. consultar reviews propias paginadas por fecha descendente
        Page<Review> reviewPage = reviewRepository
            .findAllByContent_TmdbIdAndContent_MediaType(
                tmdbId,
                mediaType,
                PageRequest.of(page - 1, size, Sort.by("createdAt").descending())
            );

        // 2. mapear a items con source WATCHNEXT
        List<WatchNextReviewItem> items = reviewPage.getContent()
            .stream()
            .map(r -> new WatchNextReviewItem(
                r.getId(),
                r.getUserId(),
                r.getBody(),
                r.getCreatedAt(),
                ReviewSource.WATCHNEXT
            ))
            .toList();

        return new WatchNextReviewsSection(
            items,
            page,
            reviewPage.getTotalPages(),
            reviewPage.getTotalElements()
        );
    }

    private TmdbReviewsSection buildTmdbSection(
        MediaType mediaType,
        Integer tmdbId,
        int page
    ) {
        try {
            // 1. llamar a content-service segun tipo de contenido
            TmdbReviewPage tmdbPage = mediaType == MediaType.MOVIE
                ? contentClient.getMovieReviews(tmdbId, page)
                : contentClient.getTvReviews(tmdbId, page);

            // 2. mapear a items con source TMDB
            List<TmdbReviewItem> items = tmdbPage.results()
                .stream()
                .map(r -> new TmdbReviewItem(
                    r.id(),
                    r.author(),
                    r.content(),
                    Instant.parse(r.createdAt()),
                    r.rating(),
                    ReviewSource.TMDB
                ))
                .toList();

            return new TmdbReviewsSection(
                items,
                tmdbPage.page(),
                tmdbPage.totalPages(),
                tmdbPage.totalResults(),
                true
            );
        } catch (Exception e) {
            // 1. fallback: seccion no disponible, sin propagar error
            return new TmdbReviewsSection(
                List.of(),
                page,
                0,
                0,
                false
            );
        }
    }
}