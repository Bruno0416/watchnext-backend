package com.watchnext.content_service.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.watchnext.content_service.controller.movies.MoviesController;
import com.watchnext.content_service.dto.common.CastMember;
import com.watchnext.content_service.dto.common.Genre;
import com.watchnext.content_service.dto.common.Video;
import com.watchnext.content_service.dto.movies.MovieDetails;
import com.watchnext.content_service.service.content.ContentService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class MoviesControllerTest {

    @Mock
    ContentService contentService;

    MockMvc mockMvc;

    private static final MovieDetails SAMPLE = new MovieDetails(
        550,
        "Fight Club",
        "Fight Club",
        "Overview",
        "/poster.jpg",
        "/backdrop.jpg",
        "1999-10-15",
        139,
        8.4,
        26000,
        List.of(new Genre(18, "Drama")),
        List.of(
            new CastMember(
                819L,
                "Edward Norton",
                "The Narrator",
                "/edward.jpg",
                0
            )
        ),
        List.of(
            new Video(
                "abc",
                "SUXWAEX2jlg",
                "YouTube",
                "Trailer",
                "Official Trailer",
                true
            )
        )
    );

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            new MoviesController(contentService)
        ).build();
    }

    @Test
    void getMovieDetails_returns200WithCastAndVideos() throws Exception {
        when(contentService.getMovieDetails(anyInt(), anyString())).thenReturn(
            Mono.just(SAMPLE)
        );

        MvcResult mvcResult = mockMvc
            .perform(
                get("/api/v1/content/movies/550").accept(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc
            .perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(550))
            .andExpect(jsonPath("$.title").value("Fight Club"))
            .andExpect(jsonPath("$.cast").isArray())
            .andExpect(jsonPath("$.cast[0].name").value("Edward Norton"))
            .andExpect(jsonPath("$.cast[0].profilePath").value("/edward.jpg"))
            .andExpect(jsonPath("$.videos").isArray())
            .andExpect(jsonPath("$.videos[0].site").value("YouTube"))
            .andExpect(jsonPath("$.videos[0].type").value("Trailer"));
    }

    @Test
    void getMovieDetails_defaultLanguageIsEnUs() throws Exception {
        when(contentService.getMovieDetails(550, "en-US")).thenReturn(
            Mono.just(SAMPLE)
        );

        MvcResult mvcResult = mockMvc
            .perform(
                get("/api/v1/content/movies/550").accept(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk());
    }

    @Test
    void getMovieDetails_notFound_returns404() throws Exception {
        when(contentService.getMovieDetails(anyInt(), anyString())).thenReturn(
            Mono.empty()
        );

        MvcResult mvcResult = mockMvc
            .perform(
                get("/api/v1/content/movies/9999").accept(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc
            .perform(asyncDispatch(mvcResult))
            .andExpect(status().isNotFound());
    }
}
