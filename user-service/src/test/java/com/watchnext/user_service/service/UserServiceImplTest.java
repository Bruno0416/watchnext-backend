package com.watchnext.user_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.watchnext.common.context.UserContext;
import com.watchnext.user_service.client.ContentServiceClient;
import com.watchnext.user_service.entity.Profile;
import com.watchnext.user_service.exception.CountryNotFound;
import com.watchnext.user_service.mapper.ContentRefMapper;
import com.watchnext.user_service.mapper.ProfileMapper;
import com.watchnext.user_service.repository.FollowRepository;
import com.watchnext.user_service.repository.ProfileRepository;
import com.watchnext.user_service.service.avatar.AvatarService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// getUserContext() debe hidratar pais y region reales del perfil (no hardcodear
// region="DEFAULT"), con fallback null-safe a US/DEFAULT si el perfil no tiene
// pais o region persistidos. Ver UserServiceImpl.java.
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private ProfileRepository profileRepo;

    @Mock
    private FollowRepository followRepo;

    @Mock
    private ProfileMapper mapper;

    @Mock
    private ContentRefMapper contentRefMapper;

    @Mock
    private ContentServiceClient contentClient;

    @Mock
    private AvatarService avatarService;

    private UserServiceImpl userService;

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(
            profileRepo,
            followRepo,
            mapper,
            contentRefMapper,
            contentClient,
            avatarService
        );
    }

    // Codigo: perfil con pais y region persistidos -> UserContext con ambos valores reales
    @Test
    public void testGetUserContextReturnsPersistedCountryAndRegion() {
        // 1. preparar perfil con pais y region
        Profile profile = Profile.builder()
            .userId("user-cl-1")
            .username("clientecl")
            .country("CL")
            .region("LATAM")
            .build();
        when(profileRepo.findByUserId("user-cl-1")).thenReturn(Optional.of(profile));

        // 2. ejecutar test
        UserContext context = userService.getUserContext("user-cl-1");

        // 3. verificar
        assertThat(context.country()).isEqualTo("CL");
        assertThat(context.region()).isEqualTo("LATAM");
    }

    // Codigo: region nula en el perfil -> fallback a DEFAULT
    @Test
    public void testGetUserContextNullRegionFallsBackToDefault() {
        // 1. preparar perfil sin region
        Profile profile = Profile.builder()
            .userId("user-cl-2")
            .username("sinregion")
            .country("CL")
            .build();
        when(profileRepo.findByUserId("user-cl-2")).thenReturn(Optional.of(profile));

        // 2. ejecutar test
        UserContext context = userService.getUserContext("user-cl-2");

        // 3. verificar
        assertThat(context.country()).isEqualTo("CL");
        assertThat(context.region()).isEqualTo("DEFAULT");
    }

    // Codigo: pais nulo en el perfil -> fallback a US
    @Test
    public void testGetUserContextNullCountryFallsBackToUs() {
        // 1. preparar perfil sin pais
        Profile profile = Profile.builder()
            .userId("user-3")
            .username("sinpais")
            .region("LATAM")
            .build();
        when(profileRepo.findByUserId("user-3")).thenReturn(Optional.of(profile));

        // 2. ejecutar test
        UserContext context = userService.getUserContext("user-3");

        // 3. verificar
        assertThat(context.country()).isEqualTo("US");
        assertThat(context.region()).isEqualTo("LATAM");
    }

    // Codigo: perfil inexistente -> CountryNotFound
    @Test
    public void testGetUserContextProfileNotFoundThrows() {
        // 1. preparar ausencia de perfil
        when(profileRepo.findByUserId("no-such-user")).thenReturn(Optional.empty());

        // 2. ejecutar y verificar
        assertThatThrownBy(() -> userService.getUserContext("no-such-user"))
            .isInstanceOf(CountryNotFound.class);
    }
}
