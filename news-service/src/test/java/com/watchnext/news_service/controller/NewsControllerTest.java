package com.watchnext.news_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchnext.common.exceptions.GlobalExceptionHandler;
import com.watchnext.news_service.dto.NewsRequest;
import com.watchnext.news_service.dto.NewsResponse;
import com.watchnext.news_service.exceptions.NewsNotFound;
import com.watchnext.news_service.exceptions.SlugAlreadyExists;
import com.watchnext.news_service.service.NewsService;
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

@WebMvcTest(NewsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NewsService service;

    private final UUID id = UUID.fromString("7d1c2a10-0001-4c3e-9a51-000000000001");

    private NewsResponse noticia() {
        return new NewsResponse(
            id,
            "cuatro-titulos",
            "Cuatro títulos",
            "Resumen",
            "Contenido",
            null,
            Instant.parse("2026-10-01T15:00:00Z")
        );
    }

    private NewsRequest request(String slug) {
        return new NewsRequest("Cuatro títulos", slug, "Resumen", "Contenido", null, null);
    }

    // -------------- 1. CONSULTAS --------------

    @Test
    void testGetAll_ok() throws Exception {
        // 1. preparar respuesta
        when(service.getAll()).thenReturn(List.of(noticia()));

        // 2. ejecutar y verificar
        // 200
        mockMvc
            .perform(get("/api/v1/news"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].slug").value("cuatro-titulos"))
            .andExpect(jsonPath("$[0].publishedAt").value("2026-10-01T15:00:00Z"));
    }

    @Test
    void testGetById_noExiste_404() throws Exception {
        // 1. preparar error
        when(service.getById(id)).thenThrow(new NewsNotFound(id.toString()));

        // 2. ejecutar y verificar
        // 404
        mockMvc.perform(get("/api/v1/news/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void testGetBySlug_ok() throws Exception {
        // 1. preparar respuesta
        when(service.getBySlug("cuatro-titulos")).thenReturn(noticia());

        // 2. ejecutar y verificar
        // 200
        mockMvc
            .perform(get("/api/v1/news/slug/cuatro-titulos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Cuatro títulos"));
    }

    // -------------- 2. ADMINISTRACION --------------

    @Test
    void testCreate_201() throws Exception {
        // 1. preparar respuesta
        when(service.create(any())).thenReturn(noticia());

        // 2. ejecutar y verificar
        // 201
        mockMvc
            .perform(
                post("/api/v1/news")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request("cuatro-titulos")))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void testCreate_slugInvalido_400() throws Exception {
        // 1. slug con mayusculas y espacios
        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                post("/api/v1/news")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request("Cuatro Titulos")))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testCreate_slugRepetido_409() throws Exception {
        // 1. preparar error
        when(service.create(any())).thenThrow(new SlugAlreadyExists("cuatro-titulos"));

        // 2. ejecutar y verificar
        // 409
        mockMvc
            .perform(
                post("/api/v1/news")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request("cuatro-titulos")))
            )
            .andExpect(status().isConflict());
    }

    @Test
    void testUpdate_ok() throws Exception {
        // 1. preparar respuesta
        when(service.update(eq(id), any())).thenReturn(noticia());

        // 2. ejecutar y verificar
        // 200
        mockMvc
            .perform(
                put("/api/v1/news/" + id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request("cuatro-titulos")))
            )
            .andExpect(status().isOk());
    }

    @Test
    void testDelete_204() throws Exception {
        // 1. preparar
        doNothing().when(service).delete(id);

        // 2. ejecutar y verificar
        // 204
        mockMvc.perform(delete("/api/v1/news/" + id)).andExpect(status().isNoContent());
    }
}
