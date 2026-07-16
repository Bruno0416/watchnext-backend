package com.watchnext.feedback_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.watchnext.common.exceptions.GlobalExceptionHandler;
import com.watchnext.feedback_service.dto.aggregated.AggregatedReviewsResponse;
import com.watchnext.feedback_service.service.review.AggregatedReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AggregatedReviewController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva filtro JWT y seguridad para ejecutar el test
@Import(GlobalExceptionHandler.class)
public class AggregatedReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AggregatedReviewService aggregatedReviewService;

    // -------------- GET AGGREGATED REVIEWS --------------

    // Codigo 200
    @Test
    public void testGetAggregatedReviews() throws Exception {
        // 1. preparar respuesta
        AggregatedReviewsResponse response = new AggregatedReviewsResponse(null, null);

        // 2. ejecutar test
        when(aggregatedReviewService.getAggregatedReviews(any(), anyInt(), anyInt(), anyInt())).thenReturn(response);

        mockMvc
            .perform(get("/api/v1/feedback/reviews/MOVIE/123"))
            .andExpect(status().isOk());
    }
}
