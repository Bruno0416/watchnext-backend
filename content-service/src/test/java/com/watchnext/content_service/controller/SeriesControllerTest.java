package com.watchnext.content_service.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.watchnext.content_service.controller.tv.SeriesController;
import com.watchnext.content_service.dto.common.AlternativeTitle;
import com.watchnext.content_service.dto.common.CastMember;
import com.watchnext.content_service.dto.common.CrewMember;
import com.watchnext.content_service.dto.common.ExternalIds;
import com.watchnext.content_service.dto.common.Genre;
import com.watchnext.content_service.dto.common.MediaSummary;
import com.watchnext.content_service.dto.common.Video;
import com.watchnext.content_service.dto.tv.TvDetails;
import com.watchnext.content_service.dto.tv.TvEpisode;
import com.watchnext.content_service.dto.tv.TvListResponse;
import com.watchnext.content_service.dto.tv.TvSeason;
import com.watchnext.content_service.dto.tv.TvSeasonDetail;
import com.watchnext.content_service.service.content.ContentService;
import java.util.Collections;
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
class SeriesControllerTest {

    @Mock
    ContentService contentService;

    MockMvc mockMvc;

    private static final TvDetails SAMPLE_TV = new TvDetails(
        1396,
        "Breaking Bad",
        "A show",
        "/poster.jpg",
        "/backdrop.jpg",
        "2008-01-20",
        9.5,
        62,
        5,
        "Ended",
        List.of(new Genre(18, "Drama")),
        List.of(new TvSeason(100, "Season 1", "Desc", 1, 7, "/s1.jpg")),
        List.of(
            new CastMember(
                17419L,
                "Bryan Cranston",
                "Walter White",
                "/bc.jpg",
                0
            )
        ),
        List.of(
            new Video(
                "tv001",
                "BB_TRAILER",
                "YouTube",
                "Trailer",
                "Official Trailer",
                true
            )
        ),
        List.of(
            new CrewMember(66633L, "Vince Gilligan", "Creator", "/vg.jpg")
        ),
        new ExternalIds("tt0903747", null, null, null),
        List.of(
            new Genre(80, "crime")
        ),
        List.of(
            new AlternativeTitle("BR", "Breaking Bad: A Química do Mal", "pt")
        ),
        List.of(
            new MediaSummary(1399L, "Better Call Saul", "/bcs.jpg", 8.5, "tv", "2015-02-08")
        ),
        List.of(
            new MediaSummary(1400L, "Ozark", "/ozark.jpg", 8.4, "tv", "2017-07-21")
        )
    );

    private static final TvSeasonDetail SAMPLE_SEASON = new TvSeasonDetail(
        100,
        "Season 1",
        "Season overview",
        1,
        "/s1.jpg",
        List.of(
            new TvEpisode(62085, "Pilot", "Overview 1", 1, 1, "/ep1.jpg",
                8.0, 120, "2008-01-20", 58,
                List.of(new CrewMember(1223L, "Vince Gilligan", "Director", "/vg.jpg")),
                List.of(new CastMember(924L, "John Koyama", "Emilio", "/jk.jpg", 1))
            ),
            new TvEpisode(62086, "Cat's in the Bag", "Overview 2", 2, 1, null,
                7.5, 80, "2008-01-27", 48,
                Collections.emptyList(), Collections.emptyList()
            )
        ),
        8.7
    );

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            new SeriesController(contentService)
        ).build();
    }

    // -----------------------------------------------------------------------
    // TV Detail
    // -----------------------------------------------------------------------

    @Test
    void getTvDetails_returns200WithCastAndVideos() throws Exception {
        when(contentService.getTvDetails(anyInt(), anyString())).thenReturn(
            Mono.just(SAMPLE_TV)
        );

        MvcResult result = mockMvc
            .perform(
                get("/api/v1/content/tv/1396").accept(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc
            .perform(asyncDispatch(result))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1396))
            .andExpect(jsonPath("$.cast").isArray())
            .andExpect(jsonPath("$.cast[0].name").value("Bryan Cranston"))
            .andExpect(jsonPath("$.videos").isArray())
            .andExpect(jsonPath("$.videos[0].type").value("Trailer"))
            .andExpect(jsonPath("$.seasons[0].seasonNumber").value(1));
    }

    @Test
    void getTvDetails_seasonsHaveNoEpisodesField() throws Exception {
        when(contentService.getTvDetails(anyInt(), anyString())).thenReturn(
            Mono.just(SAMPLE_TV)
        );

        MvcResult result = mockMvc
            .perform(
                get("/api/v1/content/tv/1396").accept(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc
            .perform(asyncDispatch(result))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.seasons[0].episodes").doesNotExist());
    }

    @Test
    void getTvDetails_defaultLanguageIsEnUs() throws Exception {
        when(contentService.getTvDetails(1396, "en-US")).thenReturn(
            Mono.just(SAMPLE_TV)
        );

        MvcResult result = mockMvc
            .perform(
                get("/api/v1/content/tv/1396").accept(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());
    }

    // -----------------------------------------------------------------------
    // List endpoints regression
    // -----------------------------------------------------------------------

    @Test
    void getPopularTv_regression_returns200() throws Exception {
        when(contentService.getPopularTv(anyInt(), anyString())).thenReturn(
            Mono.just(new TvListResponse(1, Collections.emptyList(), 1, 0))
        );

        MvcResult result = mockMvc
            .perform(
                get("/api/v1/content/tv/popular").accept(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());
    }

    @Test
    void getOnTheAir_regression_returns200() throws Exception {
        when(contentService.getOnTheAir(anyInt(), anyString())).thenReturn(
            Mono.just(new TvListResponse(1, Collections.emptyList(), 1, 0))
        );

        MvcResult result = mockMvc
            .perform(
                get("/api/v1/content/tv/on-the-air").accept(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());
    }

    @Test
    void getTopRatedTv_regression_returns200() throws Exception {
        when(contentService.getTopRatedTv(anyInt(), anyString())).thenReturn(
            Mono.just(new TvListResponse(1, Collections.emptyList(), 1, 0))
        );

        MvcResult result = mockMvc
            .perform(
                get("/api/v1/content/tv/top-rated").accept(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());
    }

    // -----------------------------------------------------------------------
    // Season endpoint (Phase 3)
    // -----------------------------------------------------------------------

    @Test
    void getTvSeasonDetail_returns200WithEpisodes() throws Exception {
        when(
            contentService.getTvSeasonDetail(anyInt(), anyInt(), anyString())
        ).thenReturn(Mono.just(SAMPLE_SEASON));

        MvcResult result = mockMvc
            .perform(
                get("/api/v1/content/tv/1396/season/1").accept(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc
            .perform(asyncDispatch(result))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.seasonNumber").value(1))
            .andExpect(jsonPath("$.episodes").isArray())
            .andExpect(jsonPath("$.episodes[0].episode_number").value(1))
            .andExpect(jsonPath("$.episodes[0].name").value("Pilot"));
    }

    @Test
    void getTvSeasonDetail_defaultLanguageIsEnUs() throws Exception {
        when(contentService.getTvSeasonDetail(1396, 1, "en-US")).thenReturn(
            Mono.just(SAMPLE_SEASON)
        );

        MvcResult result = mockMvc
            .perform(
                get("/api/v1/content/tv/1396/season/1").accept(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());
    }

    @Test
    void getTvSeasonDetail_notFound_returns404() throws Exception {
        when(
            contentService.getTvSeasonDetail(anyInt(), anyInt(), anyString())
        ).thenReturn(Mono.empty());

        MvcResult result = mockMvc
            .perform(
                get("/api/v1/content/tv/9999/season/99").accept(
                    MediaType.APPLICATION_JSON
                )
            )
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(result)).andExpect(status().isNotFound());
    }
}
