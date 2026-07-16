package com.watchnext.user_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchnext.common.exceptions.GlobalExceptionHandler;
import com.watchnext.user_service.dto.InternalFollowingResponse;
import com.watchnext.user_service.dto.ProfileSummaryResponse;
import com.watchnext.user_service.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InternalUserController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva filtro JWT y seguridad para ejecutar el test
@Import({GlobalExceptionHandler.class, com.watchnext.common.config.converters.StringToEnumConverterFactory.class})
public class InternalUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService service;

    @MockitoBean
    private com.watchnext.common.security.internal.ServiceTokenAuthenticationFilter serviceTokenAuthenticationFilter;

    @MockitoBean
    private com.watchnext.common.security.internal.ServiceTokenProvider serviceTokenProvider;

    // -------------- GET FOLLOWING --------------

    // Codigo 200
    @Test
    public void testGetFollowing() throws Exception {
        // 1. preparar respuesta
        InternalFollowingResponse response = new InternalFollowingResponse(List.of("user-1", "user-2"));

        // 2. ejecutar test
        when(service.getFollowingUserIds(anyString())).thenReturn(response);

        mockMvc
            .perform(get("/api/v1/users/internal/user-123/following"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userIds").isArray());
    }

    // -------------- BULK GET PROFILES --------------

    // Codigo 200
    @Test
    public void testBulkGetProfiles() throws Exception {
        // 1. preparar request y respuesta
        List<String> request = List.of("user-1", "user-2");
        List<ProfileSummaryResponse> response = List.of(
            new ProfileSummaryResponse("user-1", "user1", "User One", "url1")
        );

        // 2. ejecutar test
        when(service.bulkGetProfiles(any())).thenReturn(response);

        mockMvc
            .perform(
                post("/api/v1/users/internal/bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    // -------------- SEARCH PUBLIC PROFILES --------------

    // Codigo 200
    @Test
    public void testSearchPublicProfiles() throws Exception {
        // 1. preparar respuesta
        Page<ProfileSummaryResponse> response = new PageImpl<>(List.of(
            new ProfileSummaryResponse("user-1", "user1", "User One", "url1")
        ));

        // 2. ejecutar test
        when(service.searchPublicProfiles(anyString(), any())).thenReturn(response);

        mockMvc
            .perform(get("/api/v1/users/internal/search").param("q", "test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
}
