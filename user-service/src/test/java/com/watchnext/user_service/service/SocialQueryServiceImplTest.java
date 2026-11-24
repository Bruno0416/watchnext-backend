package com.watchnext.user_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.watchnext.common.exceptions.WatchNextException;
import com.watchnext.common.model.User;
import com.watchnext.common.security.UserAuthenticationToken;
import com.watchnext.user_service.dto.RelationshipResponse;
import com.watchnext.user_service.entity.Follow;
import com.watchnext.user_service.entity.Profile;
import com.watchnext.user_service.enums.FollowStatus;
import com.watchnext.user_service.enums.ProfileVisibility;
import com.watchnext.user_service.mapper.ProfileMapper;
import com.watchnext.user_service.repository.FollowRepository;
import com.watchnext.user_service.repository.ProfileRepository;
import com.watchnext.user_service.service.social.SocialQueryServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class SocialQueryServiceImplTest {

    @Mock private ProfileRepository profileRepo;
    @Mock private FollowRepository followRepo;
    @Mock private ProfileMapper mapper;

    @InjectMocks private SocialQueryServiceImpl service;

    private final Profile me = Profile.builder().id(UUID.randomUUID()).userId("yo").username("yo").build();

    @BeforeEach
    void sesion() {
        // 1. usuario de la sesion para CurrentUser
        User user = new User("yo", "yo", "yo@mail.com", "USER");
        SecurityContextHolder.getContext().setAuthentication(new UserAuthenticationToken(user, null, List.of()));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void relacionMarcaSolicitudPendienteYQueMeSigue() {
        Profile ana = Profile.builder().id(UUID.randomUUID()).userId("ana").username("ana").build();
        when(profileRepo.findByUserId("yo")).thenReturn(Optional.of(me));
        when(profileRepo.findByUsername("ana")).thenReturn(Optional.of(ana));
        when(followRepo.findByFollowerAndFollowee(me, ana))
            .thenReturn(Optional.of(Follow.builder().follower(me).followee(ana).status(FollowStatus.PENDING).build()));
        when(followRepo.findByFollowerAndFollowee(ana, me))
            .thenReturn(Optional.of(Follow.builder().follower(ana).followee(me).status(FollowStatus.ACCEPTED).build()));

        RelationshipResponse result = service.getRelationship("ana");

        assertThat(result.following()).isFalse();
        assertThat(result.requested()).isTrue();
        assertThat(result.followsYou()).isTrue();
    }

    @Test
    void seguidoresDePerfilPrivadoSinSeguirloDa403() {
        Profile privado = Profile.builder().id(UUID.randomUUID()).userId("p").username("privado")
            .visibility(ProfileVisibility.PRIVATE).build();
        when(profileRepo.findByUsername("privado")).thenReturn(Optional.of(privado));
        when(profileRepo.findByUserId("yo")).thenReturn(Optional.of(me));
        when(followRepo.findByFollowerAndFollowee(me, privado)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFollowers("privado", Pageable.unpaged()))
            .isInstanceOf(WatchNextException.class)
            .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }
}
