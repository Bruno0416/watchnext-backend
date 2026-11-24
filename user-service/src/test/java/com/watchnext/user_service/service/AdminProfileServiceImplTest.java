package com.watchnext.user_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.watchnext.user_service.dto.AdminProfileResponse;
import com.watchnext.user_service.dto.InternalIdentityRequest;
import com.watchnext.user_service.entity.Follow;
import com.watchnext.user_service.entity.Profile;
import com.watchnext.user_service.enums.FollowStatus;
import com.watchnext.user_service.exception.UsernameAlreadyTaken;
import com.watchnext.user_service.exception.UsernameReserved;
import com.watchnext.user_service.repository.FollowRepository;
import com.watchnext.user_service.repository.ProfileRepository;
import com.watchnext.user_service.service.admin.AdminProfileServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminProfileServiceImplTest {

    @Mock private ProfileRepository profileRepo;
    @Mock private FollowRepository followRepo;

    @InjectMocks private AdminProfileServiceImpl service;

    private Profile profile(String userId, String username) {
        return Profile.builder().id(UUID.randomUUID()).userId(userId).username(username).build();
    }

    // -------------- identidad --------------
    @Test
    void updateIdentityCambiaUsernameYNombre() {
        Profile ana = profile("u1", "user_123");
        when(profileRepo.findByUserId("u1")).thenReturn(Optional.of(ana));
        when(profileRepo.findByUsername("ana_cine")).thenReturn(Optional.empty());
        when(profileRepo.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminProfileResponse result = service.updateIdentity("u1", new InternalIdentityRequest("ana_cine", " Ana "));

        assertThat(result.username()).isEqualTo("ana_cine");
        assertThat(result.displayName()).isEqualTo("Ana");
    }

    @Test
    void updateIdentityRechazaUsernameDeOtro() {
        when(profileRepo.findByUserId("u1")).thenReturn(Optional.of(profile("u1", "user_123")));
        when(profileRepo.findByUsername("ocupado")).thenReturn(Optional.of(profile("u2", "ocupado")));

        assertThatThrownBy(() -> service.updateIdentity("u1", new InternalIdentityRequest("ocupado", null)))
            .isInstanceOf(UsernameAlreadyTaken.class);
    }

    @Test
    void updateIdentityRechazaUsernameReservado() {
        when(profileRepo.findByUserId("u1")).thenReturn(Optional.of(profile("u1", "user_123")));
        String reservado = UserServiceImpl.RESERVED_USERNAMES.iterator().next();

        assertThatThrownBy(() -> service.updateIdentity("u1", new InternalIdentityRequest(reservado, null)))
            .isInstanceOf(UsernameReserved.class);
    }

    // -------------- baja --------------
    @Test
    void deleteProfileDescuentaContadoresYBorra() {
        // 1. ana sigue a beto y carla sigue a ana
        Profile ana = profile("u1", "ana");
        Profile beto = profile("u2", "beto");
        Profile carla = profile("u3", "carla");
        beto.setFollowersCount(1);
        carla.setFollowingCount(1);
        Follow anaABeto = Follow.builder().follower(ana).followee(beto).status(FollowStatus.ACCEPTED).build();
        Follow carlaAAna = Follow.builder().follower(carla).followee(ana).status(FollowStatus.ACCEPTED).build();
        when(profileRepo.findByUserId("u1")).thenReturn(Optional.of(ana));
        when(followRepo.findByFollowerOrFollowee(ana, ana)).thenReturn(List.of(anaABeto, carlaAAna));

        // 2. borrar
        service.deleteProfile("u1");

        // 3. los otros pierden el contador y se borra todo
        assertThat(beto.getFollowersCount()).isZero();
        assertThat(carla.getFollowingCount()).isZero();
        verify(followRepo).deleteAll(List.of(anaABeto, carlaAAna));
        verify(profileRepo).delete(ana);
    }
}
