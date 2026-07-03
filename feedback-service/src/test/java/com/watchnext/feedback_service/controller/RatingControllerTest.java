package com.watchnext.feedback_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchnext.common.exceptions.GlobalExceptionHandler;
import com.watchnext.feedback_service.dto.rating.RatingRequest;
import com.watchnext.feedback_service.dto.rating.RatingResponse;
import com.watchnext.feedback_service.exceptions.RatingAlreadyExists;
import com.watchnext.feedback_service.exceptions.RatingNotFound;
import com.watchnext.feedback_service.service.rating.RatingService;
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

@WebMvcTest(RatingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RatingService service;

    // -------------- 1. CREATE RATING --------------

    @Test
    void testCreateRating_success() throws Exception {
        // 1. preparar request
        var request = new RatingRequest(
            1,
            com.watchnext.common.enums.MediaType.MOVIE,
            8.5
        );
        doNothing().when(service).create(any());

        // 2. ejecutar y verificar
        // 201
        mockMvc
            .perform(
                post("/api/v1/feedback/ratings/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void testCreateRating_tmdbIdNulo_400() throws Exception {
        // 1. preparar request (tmdbId nulo)
        var request = new RatingRequest(
            null,
            com.watchnext.common.enums.MediaType.MOVIE,
            8.5
        );

        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                post("/api/v1/feedback/ratings/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateRating_scoreNulo_400() throws Exception {
        // 1. preparar request (score nulo)
        var request = new RatingRequest(
            1,
            com.watchnext.common.enums.MediaType.MOVIE,
            null
        );

        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                post("/api/v1/feedback/ratings/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateRating_calificacionYaExiste_409() throws Exception {
        // 1. preparar request
        var request = new RatingRequest(
            1,
            com.watchnext.common.enums.MediaType.MOVIE,
            8.5
        );
        doThrow(new RatingAlreadyExists()).when(service).create(any());

        // 2. ejecutar y verificar
        // 409
        mockMvc
            .perform(
                post("/api/v1/feedback/ratings/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isConflict());
    }

    // -------------- 2. MY RATINGS --------------

    @Test
    void testMyRatings_success() throws Exception {
        // 1. preparar respuesta
        var response = List.of(
            new RatingResponse(
                UUID.randomUUID(),
                1,
                com.watchnext.common.enums.MediaType.MOVIE,
                8.5,
                Instant.now(),
                Instant.now()
            )
        );
        when(service.myRatings()).thenReturn(response);

        // 2. ejecutar y verificar
        // 200
        mockMvc
            .perform(get("/api/v1/feedback/ratings/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    // -------------- 3. GET RATING --------------

    @Test
    void testGetRating_success() throws Exception {
        // 1. preparar respuesta
        var response = new RatingResponse(
            UUID.randomUUID(),
            1,
            com.watchnext.common.enums.MediaType.MOVIE,
            8.5,
            Instant.now(),
            Instant.now()
        );
        when(service.getRating(any(), any())).thenReturn(response);

        // 2. ejecutar y verificar
        // 200
        mockMvc
            .perform(
                get("/api/v1/feedback/ratings")
                    .param("tmdbId", "1")
                    .param("mediaType", "MOVIE")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tmdbId").value(1));
    }

    @Test
    void testGetRating_tmdbIdNegativo_400() throws Exception {
        // 2. ejecutar y verificar (tmdbId viola @Positive)
        // 400
        mockMvc
            .perform(
                get("/api/v1/feedback/ratings")
                    .param("tmdbId", "-1")
                    .param("mediaType", "MOVIE")
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testGetRating_noEncontrado_404() throws Exception {
        // 1. preparar
        when(service.getRating(any(), any())).thenThrow(new RatingNotFound());

        // 2. ejecutar y verificar
        // 404
        mockMvc
            .perform(
                get("/api/v1/feedback/ratings")
                    .param("tmdbId", "1")
                    .param("mediaType", "MOVIE")
            )
            .andExpect(status().isNotFound());
    }

    // -------------- 4. DELETE RATING --------------

    @Test
    void testDeleteRating_success() throws Exception {
        // 1. preparar
        doNothing().when(service).deleteRating(any(), any());

        // 2. ejecutar y verificar
        // 204
        mockMvc
            .perform(
                delete("/api/v1/feedback/ratings")
                    .param("tmdbId", "1")
                    .param("mediaType", "MOVIE")
            )
            .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteRating_tmdbIdNegativo_400() throws Exception {
        // 2. ejecutar y verificar (tmdbId viola @Positive)
        // 400
        mockMvc
            .perform(
                delete("/api/v1/feedback/ratings")
                    .param("tmdbId", "-1")
                    .param("mediaType", "MOVIE")
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteRating_noEncontrado_404() throws Exception {
        // 1. preparar
        doThrow(new RatingNotFound()).when(service).deleteRating(any(), any());

        // 2. ejecutar y verificar
        // 404
        mockMvc
            .perform(
                delete("/api/v1/feedback/ratings")
                    .param("tmdbId", "1")
                    .param("mediaType", "MOVIE")
            )
            .andExpect(status().isNotFound());
    }
}
