package com.watchnext.auth_service.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.watchnext.auth_service.client.UserDataCleanupClient;
import com.watchnext.auth_service.client.UserServiceClient;
import com.watchnext.auth_service.dto.admin.AdminCreateUserRequest;
import com.watchnext.auth_service.dto.admin.AdminProfileData;
import com.watchnext.auth_service.dto.admin.AdminUserResponse;
import com.watchnext.auth_service.entity.Users;
import com.watchnext.auth_service.enums.Role;
import com.watchnext.auth_service.exceptions.EmailAlreadyInUse;
import com.watchnext.auth_service.repository.UserIdentityRepository;
import com.watchnext.auth_service.repository.UserRepository;
import com.watchnext.auth_service.repository.VerificationCodeRepository;
import com.watchnext.common.exceptions.WatchNextException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock private UserRepository userRepo;
    @Mock private UserIdentityRepository identityRepo;
    @Mock private VerificationCodeRepository codeRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserServiceClient userServiceClient;
    @Mock private UserDataCleanupClient cleanupClient;

    @InjectMocks private AdminUserServiceImpl service;

    private Users user(Role role) {
        return Users.builder().id(UUID.randomUUID()).email("ana@mail.com").password("x").role(role).emailVerified(true).build();
    }

    // -------------- busqueda --------------
    @Test
    void searchJuntaPerfilYFiltraPorUsername() {
        // 1. dos cuentas, solo una con perfil que coincide
        Users ana = user(Role.USER);
        Users otro = Users.builder().id(UUID.randomUUID()).email("otro@mail.com").role(Role.ADMIN).build();
        when(userRepo.findAll(any(Sort.class))).thenReturn(List.of(ana, otro));
        when(userServiceClient.getAdminProfiles(anyList())).thenReturn(List.of(
            new AdminProfileData(ana.getId().toString(), "ana_cine", "Ana", "CL", "PUBLIC", true, 2, 3, 1)
        ));

        // 2. buscar por username
        List<AdminUserResponse> result = service.search("cine");

        // 3. solo vuelve ana con los datos del perfil
        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("ana_cine");
        assertThat(result.get(0).followersCount()).isEqualTo(2);
    }

    // -------------- alta --------------
    @Test
    void createRechazaCorreoOcupado() {
        when(userRepo.existsByEmail("ana@mail.com")).thenReturn(true);
        assertThatThrownBy(() -> service.create(new AdminCreateUserRequest("Ana@mail.com", "clave1234", Role.USER)))
            .isInstanceOf(EmailAlreadyInUse.class);
    }

    @Test
    void createGuardaCuentaVerificadaYCreaPerfil() {
        // 1. preparar
        when(userRepo.existsByEmail("nuevo@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("clave1234")).thenReturn("hash");
        when(userRepo.save(any(Users.class))).thenAnswer(inv -> {
            Users saved = inv.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // 2. crear
        AdminUserResponse result = service.create(new AdminCreateUserRequest("nuevo@mail.com", "clave1234", Role.ADMIN));

        // 3. queda verificada, con rol y con perfil pedido a user-service
        assertThat(result.emailVerified()).isTrue();
        assertThat(result.role()).isEqualTo("ADMIN");
        verify(userServiceClient).provisionProfile(UUID.fromString(result.userId()));
    }

    // -------------- baja --------------
    @Test
    void deleteNoPermiteBorrarseASiMismo() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> service.delete(id, id.toString()))
            .isInstanceOf(WatchNextException.class)
            .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userRepo, never()).delete(any());
    }

    @Test
    void deleteNoPermiteBorrarElUltimoAdmin() {
        Users admin = user(Role.ADMIN);
        when(userRepo.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(userRepo.countByRole(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.delete(admin.getId(), UUID.randomUUID().toString()))
            .isInstanceOf(WatchNextException.class)
            .extracting("status").isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void deleteBorraDatosPerfilYCuenta() {
        Users ana = user(Role.USER);
        when(userRepo.findById(ana.getId())).thenReturn(Optional.of(ana));

        service.delete(ana.getId(), UUID.randomUUID().toString());

        verify(cleanupClient).deleteUserData(ana.getId());
        verify(userServiceClient).deleteProfile(ana.getId());
        verify(identityRepo).deleteAllByUser(ana);
        verify(codeRepo).deleteAllByEmail("ana@mail.com");
        verify(userRepo).delete(ana);
    }
}
