package com.watchnext.feedback_service.controller;

import com.watchnext.common.enums.MediaType;
import com.watchnext.feedback_service.dto.aggregated.AggregatedReviewsResponse;
import com.watchnext.feedback_service.service.review.AggregatedReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feedback/reviews")
public class AggregatedReviewController {

    private final AggregatedReviewService aggregatedReviewService;

    @GetMapping("/{mediaType}/{tmdbId}")
    public ResponseEntity<AggregatedReviewsResponse> getAggregatedReviews(
        @PathVariable MediaType mediaType,
        @PathVariable Integer tmdbId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        // 1. delegar al servicio de agregacion
        return ResponseEntity.ok(
            aggregatedReviewService.getAggregatedReviews(
                mediaType, tmdbId, page, size
            )
        );
    }
}