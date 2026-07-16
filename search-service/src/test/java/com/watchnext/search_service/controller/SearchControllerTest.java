package com.watchnext.search_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.watchnext.common.config.converters.StringToEnumConverterFactory;
import com.watchnext.common.dto.internal.PageResponse;
import com.watchnext.search_service.dto.ContentSearchSection;
import com.watchnext.search_service.dto.SearchType;
import com.watchnext.search_service.dto.UnifiedSearchResponse;
import com.watchnext.search_service.service.SearchAggregator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.format.support.FormattingConversionService;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    SearchAggregator aggregator;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FormattingConversionService conversionService = new FormattingConversionService();
        conversionService.addConverterFactory(new StringToEnumConverterFactory());

        // 1. construir mockmvc con el controlador bajo prueba
        mockMvc = MockMvcBuilders
            .standaloneSetup(new SearchController(aggregator))
            .setConversionService(conversionService)
            .build();
    }

    // -------------- SEARCH --------------

    // Codigo 200
    @Test
    void testSearch() throws Exception {
        // 1. preparar respuesta
        UnifiedSearchResponse response = new UnifiedSearchResponse(
            new ContentSearchSection("test", "test", false, 0, List.of()),
            new PageResponse<>(List.of(), 1, 20, 0, 0),
            true,
            true
        );

        // 2. ejecutar test
        when(aggregator.search(anyString(), any(), anyInt())).thenReturn(Mono.just(response));

        MvcResult result = mockMvc
            .perform(get("/api/v1/search").param("q", "test"))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc
            .perform(asyncDispatch(result))
            .andExpect(status().isOk());
    }

    // -------------- SEARCH MISSING QUERY --------------

    // Codigo 400
    @Test
    void testSearchMissingQuery() throws Exception {
        // 1. ejecutar test sin query param "q"
        mockMvc.perform(get("/api/v1/search")).andExpect(status().isBadRequest());
    }

    // -------------- SEARCH @ PREFIX FUERZA SOLO USUARIOS --------------

    @Test
    void testSearchAtPrefixForcesUserOnlyAndStripsAt() throws Exception {
        // 1. preparar respuesta
        UnifiedSearchResponse response = new UnifiedSearchResponse(
            null,
            new PageResponse<>(List.of(), 1, 20, 0, 0),
            false,
            true
        );
        when(
            aggregator.search(eq("bruno"), eq(Set.of(SearchType.USER)), anyInt())
        ).thenReturn(Mono.just(response));

        // 2. ejecutar test con q="@bruno"
        MvcResult result = mockMvc
            .perform(get("/api/v1/search").param("q", "@bruno"))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc
            .perform(asyncDispatch(result))
            .andExpect(status().isOk());
    }

    // -------------- SEARCH TYPES CON ALIAS --------------

    @Test
    void testSearchTypesAcceptsCaseInsensitiveEnums() throws Exception {
        // 1. preparar respuesta
        UnifiedSearchResponse response = new UnifiedSearchResponse(
            new ContentSearchSection("batman", "batman", false, 0, List.of()),
            null,
            true,
            false
        );
        when(
            aggregator.search(
                anyString(),
                eq(Set.of(SearchType.MOVIE, SearchType.TV)),
                anyInt()
            )
        ).thenReturn(Mono.just(response));

        // 2. ejecutar test con types "movie,tV" (case insensitive)
        MvcResult result = mockMvc
            .perform(
                get("/api/v1/search")
                    .param("q", "batman")
                    .param("types", "movie,tV")
            )
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc
            .perform(asyncDispatch(result))
            .andExpect(status().isOk());
    }
}
