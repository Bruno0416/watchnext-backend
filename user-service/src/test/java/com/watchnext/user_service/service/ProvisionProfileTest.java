package com.watchnext.user_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.watchnext.user_service.client.ContentServiceClient;
import com.watchnext.user_service.entity.Profile;
import com.watchnext.user_service.enums.ProfileVisibility;
import com.watchnext.user_service.mapper.ContentRefMapper;
import com.watchnext.user_service.mapper.ProfileMapper;
import com.watchnext.user_service.repository.FollowRepository;
import com.watchnext.user_service.repository.ProfileRepository;
import com.watchnext.user_service.service.avatar.AvatarService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// provisionProfile(): auth-service lo llama al registrar un usuario nuevo
@ExtendWith(MockitoExtension.class)
public class ProvisionProfileTest {

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

    private final String authUserId = "2e29b7a1-07c3-5e46-8d0f-745b4f280aa7";

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

    // Codigo: usuario nuevo -> perfil privado con username temporal y onboarding pendiente
    @Test
    public void testProvisionCreaPerfilPendiente() {
        // 1. preparar: no hay perfil y el username temporal esta libre
        when(profileRepo.findByUserId(authUserId)).thenReturn(Optional.empty());
        when(profileRepo.existsByUsername(anyString())).thenReturn(false);

        // 2. ejecutar test
        userService.provisionProfile(authUserId);

        // 3. verificar el perfil guardado
        ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepo).save(captor.capture());
        Profile profile = captor.getValue();
        assertThat(profile.getUserId()).isEqualTo(authUserId);
        assertThat(profile.getUsername()).isEqualTo("user_2e29b7a107");
        assertThat(profile.getVisibility()).isEqualTo(ProfileVisibility.PRIVATE);
        assertThat(profile.isOnboardingCompleted()).isFalse();
    }

    // Codigo: username temporal tomado -> se agrega un numero
    @Test
    public void testProvisionUsernameRepetidoAgregaNumero() {
        // 1. preparar: el primer username ya existe
        when(profileRepo.findByUserId(authUserId)).thenReturn(Optional.empty());
        when(profileRepo.existsByUsername("user_2e29b7a107")).thenReturn(true);
        when(profileRepo.existsByUsername("user_2e29b7a1071")).thenReturn(false);

        // 2. ejecutar test
        userService.provisionProfile(authUserId);

        // 3. verificar
        ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepo).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("user_2e29b7a1071");
    }

    // Codigo: auth reintenta y el perfil ya existe -> no crea otro
    @Test
    public void testProvisionPerfilExistenteNoHaceNada() {
        // 1. preparar perfil existente
        Profile existente = Profile.builder().userId(authUserId).username("ana").build();
        when(profileRepo.findByUserId(authUserId)).thenReturn(Optional.of(existente));

        // 2. ejecutar test
        userService.provisionProfile(authUserId);

        // 3. verificar
        verify(profileRepo, never()).save(any());
    }
}
