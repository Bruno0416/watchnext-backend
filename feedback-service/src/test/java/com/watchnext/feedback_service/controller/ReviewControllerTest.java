package com.watchnext.feedback_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchnext.common.exceptions.GlobalExceptionHandler;
import com.watchnext.feedback_service.dto.review.ReviewRequest;
import com.watchnext.feedback_service.dto.review.ReviewResponse;
import com.watchnext.feedback_service.dto.review.ReviewUpdateRequest;
import com.watchnext.feedback_service.exceptions.ReviewAlreadyExists;
import com.watchnext.feedback_service.exceptions.ReviewNotFound;
import com.watchnext.feedback_service.service.review.ReviewService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService service;

    // -------------- 1. CREATE REVIEW --------------

    @Test
    void testCreateReview_success() throws Exception {
        // 1. preparar request
        var request = new ReviewRequest(
            1,
            com.watchnext.common.model.MediaType.MOVIE,
            "Excelente película"
        );
        doNothing().when(service).createReview(any());

        // 2. ejecutar y verificar
        // 201
        mockMvc
            .perform(
                post("/api/v1/feedback/reviews/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void testCreateReview_cuerpoEnBlanco_400() throws Exception {
        // 1. preparar request (body vacío)
        var request = new ReviewRequest(
            1,
            com.watchnext.common.model.MediaType.MOVIE,
            ""
        );

        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                post("/api/v1/feedback/reviews/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateReview_resenaYaExiste_409() throws Exception {
        // 1. preparar request
        var request = new ReviewRequest(
            1,
            com.watchnext.common.model.MediaType.MOVIE,
            "Excelente película"
        );
        doThrow(new ReviewAlreadyExists()).when(service).createReview(any());

        // 2. ejecutar y verificar
        // 409
        mockMvc
            .perform(
                post("/api/v1/feedback/reviews/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isConflict());
    }

    // -------------- 2. GET ALL CONTENT REVIEWS --------------

    @Test
    void testGetAllContentReviews_success() throws Exception {
        // 1. preparar respuesta
        var response = List.of(
            new ReviewResponse(
                UUID.randomUUID(),
                "user-id",
                1,
                com.watchnext.common.model.MediaType.MOVIE,
                "Excelente",
                Instant.now(),
                Instant.now()
            )
        );
        when(service.getAllContentReviews(any(), any())).thenReturn(response);

        // 2. ejecutar y verificar
        // 200
        mockMvc
            .perform(
                get("/api/v1/feedback/reviews")
                    .param("tmdbId", "1")
                    .param("mediaType", "MOVIE")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetAllContentReviews_tmdbIdNegativo_400() throws Exception {
        // 2. ejecutar y verificar (tmdbId viola @Positive)
        // 400
        mockMvc
            .perform(
                get("/api/v1/feedback/reviews")
                    .param("tmdbId", "-1")
                    .param("mediaType", "MOVIE")
            )
            .andExpect(status().isBadRequest());
    }

    // -------------- 3. MY REVIEWS --------------

    @Test
    void testGetMyReviews_success() throws Exception {
        // 1. preparar respuesta
        when(service.getMyReviews()).thenReturn(List.of());

        // 2. ejecutar y verificar
        // 200
        mockMvc
            .perform(get("/api/v1/feedback/reviews/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    // -------------- 4. UPDATE REVIEW --------------

    @Test
    void testUpdateReview_success() throws Exception {
        // 1. preparar request
        var id = UUID.randomUUID();
        var request = new ReviewUpdateRequest("Cuerpo actualizado");
        doNothing().when(service).updateReview(any(), any());

        // 2. ejecutar y verificar
        // 200
        mockMvc
            .perform(
                put("/api/v1/feedback/reviews/update/" + id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());
    }

    @Test
    void testUpdateReview_cuerpoEnBlanco_400() throws Exception {
        // 1. preparar request (body vacío)
        var id = UUID.randomUUID();
        var request = new ReviewUpdateRequest("");

        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                put("/api/v1/feedback/reviews/update/" + id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateReview_noEncontrada_404() throws Exception {
        // 1. preparar
        var id = UUID.randomUUID();
        var request = new ReviewUpdateRequest("Cuerpo actualizado");
        doThrow(new ReviewNotFound()).when(service).updateReview(any(), any());

        // 2. ejecutar y verificar
        // 404
        mockMvc
            .perform(
                put("/api/v1/feedback/reviews/update/" + id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isNotFound());
    }

    // -------------- 5. DELETE REVIEW --------------

    @Test
    void testDeleteReview_success() throws Exception {
        // 1. preparar
        var id = UUID.randomUUID();
        doNothing().when(service).deleteReview(any());

        // 2. ejecutar y verificar
        // 204
        mockMvc
            .perform(delete("/api/v1/feedback/reviews/" + id))
            .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteReview_noEncontrada_404() throws Exception {
        // 1. preparar
        var id = UUID.randomUUID();
        doThrow(new ReviewNotFound()).when(service).deleteReview(any());

        // 2. ejecutar y verificar
        // 404
        mockMvc
            .perform(delete("/api/v1/feedback/reviews/" + id))
            .andExpect(status().isNotFound());
    }
}
