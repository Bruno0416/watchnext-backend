package com.watchnext.auth_service.service.admin;

import com.watchnext.auth_service.client.UserDataCleanupClient;
import com.watchnext.auth_service.client.UserServiceClient;
import com.watchnext.auth_service.dto.admin.AdminCreateUserRequest;
import com.watchnext.auth_service.dto.admin.AdminIdentityRequest;
import com.watchnext.auth_service.dto.admin.AdminProfileData;
import com.watchnext.auth_service.dto.admin.AdminUserResponse;
import com.watchnext.auth_service.entity.Users;
import com.watchnext.auth_service.enums.Role;
import com.watchnext.auth_service.exceptions.EmailAlreadyInUse;
import com.watchnext.auth_service.exceptions.UserNotFound;
import com.watchnext.auth_service.repository.UserIdentityRepository;
import com.watchnext.auth_service.repository.UserRepository;
import com.watchnext.auth_service.repository.VerificationCodeRepository;
import com.watchnext.common.exceptions.WatchNextException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepo;
    private final UserIdentityRepository identityRepo;
    private final VerificationCodeRepository codeRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserServiceClient userServiceClient;
    private final UserDataCleanupClient cleanupClient;

    // ---------- consultas ----------
    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> search(String query) {
        // 1. todas las cuentas, las mas nuevas primero
        List<Users> users = userRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        // 2. juntar cada cuenta con su perfil de user-service
        Map<String, AdminProfileData> profiles = profilesOf(users.stream().map(Users::getId).toList());
        List<AdminUserResponse> result = users.stream()
            .map(user -> AdminUserResponse.of(user, profiles.get(user.getId().toString())))
            .toList();

        // 3. filtrar por correo, username o nombre visible
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) return result;
        return result.stream().filter(user -> matches(user, q)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getById(UUID userId) {
        Users user = userRepo.findById(userId).orElseThrow(UserNotFound::new);
        return withProfile(user);
    }

    // ---------- alta ----------
    @Override
    @Transactional
    public AdminUserResponse create(AdminCreateUserRequest request) {
        // 1. el correo no puede estar ocupado
        String email = request.email().trim().toLowerCase();
        if (userRepo.existsByEmail(email)) throw new EmailAlreadyInUse(email);

        // 2. la cuenta la crea un admin, queda verificada
        Users user = userRepo.save(
            Users.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .emailVerified(true)
                .build()
        );

        // 3. mismo perfil vacio que el registro normal
        userServiceClient.provisionProfile(user.getId());
        return withProfile(user);
    }

    // ---------- identidad ----------
    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse updateIdentity(UUID userId, AdminIdentityRequest request) {
        // 1. la cuenta tiene que existir en auth-service
        Users user = userRepo.findById(userId).orElseThrow(UserNotFound::new);

        // 2. user-service valida username reservado u ocupado
        AdminProfileData profile = userServiceClient.updateIdentity(userId, request);
        return AdminUserResponse.of(user, profile);
    }

    // ---------- baja ----------
    @Override
    @Transactional
    public void delete(UUID userId, String actorId) {
        // 1. el admin no se puede borrar a si mismo
        if (userId.toString().equals(actorId)) {
            throw new WatchNextException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia cuenta administrativa.");
        }

        // 2. siempre tiene que quedar al menos un admin
        Users user = userRepo.findById(userId).orElseThrow(UserNotFound::new);
        if (user.getRole() == Role.ADMIN && userRepo.countByRole(Role.ADMIN) <= 1) {
            throw new WatchNextException(HttpStatus.CONFLICT, "No puedes eliminar el ultimo administrador.");
        }

        // 3. borrar sus datos en los otros servicios
        cleanupClient.deleteUserData(userId);
        userServiceClient.deleteProfile(userId);

        // 4. borrar la cuenta y lo que cuelga de ella en auth
        identityRepo.deleteAllByUser(user);
        codeRepo.deleteAllByEmail(user.getEmail());
        userRepo.delete(user);
    }

    // ---------- helpers ----------
    private AdminUserResponse withProfile(Users user) {
        return AdminUserResponse.of(user, profilesOf(List.of(user.getId())).get(user.getId().toString()));
    }

    private Map<String, AdminProfileData> profilesOf(List<UUID> ids) {
        if (ids.isEmpty()) return Map.of();
        return userServiceClient.getAdminProfiles(ids).stream()
            .collect(Collectors.toMap(AdminProfileData::userId, Function.identity(), (a, b) -> a));
    }

    private boolean matches(AdminUserResponse user, String q) {
        return contains(user.email(), q) || contains(user.username(), q) || contains(user.displayName(), q);
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }
}
