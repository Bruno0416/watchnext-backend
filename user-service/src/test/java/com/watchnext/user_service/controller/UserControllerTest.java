package com.watchnext.user_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchnext.common.exceptions.GlobalExceptionHandler;
import com.watchnext.user_service.dto.FavoritesRequest;
import com.watchnext.user_service.dto.OnboardingRequest;
import com.watchnext.user_service.dto.ProfileResponse;
import com.watchnext.user_service.dto.ProfileSummaryResponse;
import com.watchnext.user_service.dto.UpdateProfileRequest;
import com.watchnext.user_service.dto.UsernameAvailabilityResponse;
import com.watchnext.user_service.enums.ProfileVisibility;
import com.watchnext.user_service.exception.AlreadyFollowing;
import com.watchnext.user_service.exception.ProfileNotFound;
import com.watchnext.user_service.exception.UsernameAlreadyTaken;
import com.watchnext.user_service.service.UserService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva filtro JWT y seguridad para ejecutar el test
@Import({GlobalExceptionHandler.class, com.watchnext.common.config.converters.StringToEnumConverterFactory.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // para mapear objetos/clases a json

    @MockitoBean
    private UserService service;

    @MockitoBean
    private com.watchnext.common.security.internal.ServiceTokenAuthenticationFilter serviceTokenAuthenticationFilter;

    @MockitoBean
    private com.watchnext.common.security.internal.ServiceTokenProvider serviceTokenProvider;

    // -------------- GET MY PROFILE --------------

    // Codigo 200
    @Test
    public void testGetMyProfile() throws Exception {
        // 1. preparar respuesta
        ProfileResponse response = new ProfileResponse(
            java.util.UUID.randomUUID(),
            "user-123",
            "testuser",
            "Test User",
            "bio",
            "url",
            "CL",
            ProfileVisibility.PUBLIC,
            true,
            0,
            0,
            List.of(),
            Instant.now(),
            Instant.now()
        );

        // 2. ejecutar test
        when(service.getMyProfile(anyString())).thenReturn(response);

        mockMvc
            .perform(get("/api/v1/users/me").param("language", "en-US"))
            .andExpect(status().isOk());
    }

    // -------------- COMPLETE ONBOARDING --------------

    // Codigo 204
    @Test
    public void testCompleteOnboarding() throws Exception {
        // 1. preparar request prueba
        OnboardingRequest request = new OnboardingRequest(
            "newuser",
            ProfileVisibility.PUBLIC,
            "New User",
            "bio",
            "CL",
            List.of()
        );
        MockMultipartFile dataFile = new MockMultipartFile(
            "data",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(request)
        );

        // 2. ejecutar test
        doNothing().when(service).completeOnboarding(any(), any());

        mockMvc
            .perform(
                multipart(HttpMethod.PUT, "/api/v1/users/me/onboarding")
                    .file(dataFile)
            )
            .andExpect(status().isNoContent());
    }

    // Codigo 400 - username vacio
    @Test
    public void testCompleteOnboardingInvalidFields() throws Exception {
        // 1. preparar request prueba
        OnboardingRequest request = new OnboardingRequest(
            "",
            ProfileVisibility.PUBLIC,
            "New User",
            "bio",
            "CL",
            List.of()
        );
        MockMultipartFile dataFile = new MockMultipartFile(
            "data",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(request)
        );

        // 2. ejecutar test
        mockMvc
            .perform(
                multipart(HttpMethod.PUT, "/api/v1/users/me/onboarding")
                    .file(dataFile)
            )
            .andExpect(status().isBadRequest());
    }

    // -------------- UPDATE PROFILE --------------

    // Codigo 200
    @Test
    public void testUpdateProfile() throws Exception {
        // 1. preparar request prueba
        UpdateProfileRequest request = new UpdateProfileRequest(
            "Updated",
            "new bio",
            ProfileVisibility.PRIVATE,
            "CL",
            List.of()
        );

        // 2. preparar respuesta
        ProfileResponse response = new ProfileResponse(
            java.util.UUID.randomUUID(),
            "user-123",
            "testuser",
            "Updated",
            "new bio",
            "url",
            "CL",
            ProfileVisibility.PRIVATE,
            true,
            0,
            0,
            List.of(),
            Instant.now(),
            Instant.now()
        );

        // 3. ejecutar test
        when(service.updateProfile(any(), anyString())).thenReturn(response);

        mockMvc
            .perform(
                put("/api/v1/users/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());
    }

    // Codigo 409 - Username already taken
    @Test
    public void testUpdateProfileConflict() throws Exception {
        // 1. preparar request prueba
        UpdateProfileRequest request = new UpdateProfileRequest(
            "Updated",
            "new bio",
            ProfileVisibility.PRIVATE,
            "CL",
            List.of()
        );

        // 2. ejecutar test
        doThrow(new UsernameAlreadyTaken("takenuser"))
            .when(service)
            .updateProfile(any(), anyString());

        mockMvc
            .perform(
                put("/api/v1/users/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isConflict());
    }

    // -------------- UPLOAD AVATAR --------------

    // Codigo 204
    @Test
    public void testUploadAvatar() throws Exception {
        // 1. preparar request prueba
        MockMultipartFile avatarFile = new MockMultipartFile(
            "avatar",
            "test.png",
            MediaType.IMAGE_PNG_VALUE,
            "test".getBytes()
        );

        // 2. ejecutar test
        doNothing().when(service).uploadAvatar(any());

        mockMvc
            .perform(multipart("/api/v1/users/me/avatar").file(avatarFile))
            .andExpect(status().isNoContent());
    }

    // -------------- DELETE AVATAR --------------

    // Codigo 204
    @Test
    public void testDeleteAvatar() throws Exception {
        // 1. ejecutar test
        doNothing().when(service).deleteAvatar();

        mockMvc
            .perform(delete("/api/v1/users/me/avatar"))
            .andExpect(status().isNoContent());
    }

    // -------------- REPLACE FAVORITES --------------

    // Codigo 204
    @Test
    public void testReplaceFavorites() throws Exception {
        // 1. preparar request prueba
        FavoritesRequest request = new FavoritesRequest(List.of());

        // 2. ejecutar test
        doNothing().when(service).replaceFavorites(any());

        mockMvc
            .perform(
                put("/api/v1/users/me/favorites")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isNoContent());
    }

    // -------------- CHECK USERNAME AVAILABLE --------------

    // Codigo 200
    @Test
    public void testCheckUsernameAvailable() throws Exception {
        // 1. preparar respuesta
        UsernameAvailabilityResponse response = new UsernameAvailabilityResponse(true);

        // 2. ejecutar test
        when(service.checkUsernameAvailable(anyString())).thenReturn(response);

        mockMvc
            .perform(get("/api/v1/users/username-available").param("username", "test"))
            .andExpect(status().isOk());
    }

    // -------------- GET PROFILE --------------

    // Codigo 200
    @Test
    public void testGetProfile() throws Exception {
        // 1. preparar respuesta
        ProfileResponse response = new ProfileResponse(
            java.util.UUID.randomUUID(),
            "user-123",
            "testuser",
            "Test User",
            "bio",
            "url",
            "CL",
            ProfileVisibility.PUBLIC,
            true,
            0,
            0,
            List.of(),
            Instant.now(),
            Instant.now()
        );

        // 2. ejecutar test
        when(service.getProfile(anyString(), anyString())).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/testuser")).andExpect(status().isOk());
    }

    // Codigo 404
    @Test
    public void testGetProfileNotFound() throws Exception {
        // 1. ejecutar test
        when(service.getProfile(anyString(), anyString()))
            .thenThrow(new ProfileNotFound());

        mockMvc.perform(get("/api/v1/users/testuser")).andExpect(status().isNotFound());
    }

    // -------------- SEARCH PROFILES --------------

    // Codigo 200
    @Test
    public void testSearchProfiles() throws Exception {
        // 1. preparar respuesta
        Page<ProfileSummaryResponse> response = new PageImpl<>(List.of());

        // 2. ejecutar test
        when(service.searchProfiles(anyString(), any())).thenReturn(response);

        mockMvc
            .perform(get("/api/v1/users/search").param("q", "test"))
            .andExpect(status().isOk());
    }

    // -------------- FOLLOW --------------

    // Codigo 201
    @Test
    public void testFollow() throws Exception {
        // 1. ejecutar test
        doNothing().when(service).follow(anyString());

        mockMvc
            .perform(post("/api/v1/users/testuser/follow"))
            .andExpect(status().isCreated());
    }

    // Codigo 409
    @Test
    public void testFollowConflict() throws Exception {
        // 1. ejecutar test
        doThrow(new AlreadyFollowing()).when(service).follow(anyString());

        mockMvc
            .perform(post("/api/v1/users/testuser/follow"))
            .andExpect(status().isConflict());
    }

    // -------------- UNFOLLOW --------------

    // Codigo 204
    @Test
    public void testUnfollow() throws Exception {
        // 1. ejecutar test
        doNothing().when(service).unfollow(anyString());

        mockMvc
            .perform(delete("/api/v1/users/testuser/follow"))
            .andExpect(status().isNoContent());
    }

    // -------------- GET MY FOLLOWERS --------------

    // Codigo 200
    @Test
    public void testGetMyFollowers() throws Exception {
        // 1. preparar respuesta
        Page<ProfileSummaryResponse> response = new PageImpl<>(List.of());

        // 2. ejecutar test
        when(service.getMyFollowers(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me/followers")).andExpect(status().isOk());
    }

    // -------------- GET MY FOLLOWING --------------

    // Codigo 200
    @Test
    public void testGetMyFollowing() throws Exception {
        // 1. preparar respuesta
        Page<ProfileSummaryResponse> response = new PageImpl<>(List.of());

        // 2. ejecutar test
        when(service.getMyFollowing(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me/following")).andExpect(status().isOk());
    }

    // -------------- GET FOLLOW REQUESTS --------------

    // Codigo 200
    @Test
    public void testGetFollowRequests() throws Exception {
        // 1. preparar respuesta
        Page<ProfileSummaryResponse> response = new PageImpl<>(List.of());

        // 2. ejecutar test
        when(service.getFollowRequests(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me/follow-requests")).andExpect(status().isOk());
    }

    // -------------- ACCEPT FOLLOW REQUEST --------------

    // Codigo 204
    @Test
    public void testAcceptFollowRequest() throws Exception {
        // 1. ejecutar test
        doNothing().when(service).acceptFollowRequest(anyString());

        mockMvc
            .perform(post("/api/v1/users/me/follow-requests/testuser"))
            .andExpect(status().isNoContent());
    }

    // -------------- REJECT FOLLOW REQUEST --------------

    // Codigo 204
    @Test
    public void testRejectFollowRequest() throws Exception {
        // 1. ejecutar test
        doNothing().when(service).rejectFollowRequest(anyString());

        mockMvc
            .perform(delete("/api/v1/users/me/follow-requests/testuser"))
            .andExpect(status().isNoContent());
    }
}
