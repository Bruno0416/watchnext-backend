package com.watchnext.auth_service.service.social;

import com.watchnext.auth_service.dto.AuthResponse;
import com.watchnext.auth_service.dto.social.SocialUserInfo;
import com.watchnext.auth_service.entity.UserIdentity;
import com.watchnext.auth_service.entity.Users;
import com.watchnext.auth_service.enums.AuthProvider;
import com.watchnext.auth_service.enums.Role;
import com.watchnext.auth_service.exceptions.EmailNotVerified;
import com.watchnext.auth_service.exceptions.UnsupportedProvider;
import com.watchnext.auth_service.repository.UserIdentityRepository;
import com.watchnext.auth_service.repository.UserRepository;
import com.watchnext.auth_service.security.JwtUtil;
import com.watchnext.auth_service.security.social.SocialTokenVerifier;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final UserRepository userRepo;
    private final UserIdentityRepository identityRepo;
    private final JwtUtil jwtService;
    private final List<SocialTokenVerifier> verifiers;

    private Map<AuthProvider, SocialTokenVerifier> verifiersByProvider;

    @PostConstruct
    public void init() {
        this.verifiersByProvider = verifiers
            .stream()
            .collect(
                Collectors.toMap(
                    SocialTokenVerifier::provider,
                    Function.identity()
                )
            );
    }

    @Transactional
    public AuthResponse authenticate(AuthProvider provider, String token) {
        // 1. si no hay verifier para el provider -> 400
        SocialTokenVerifier verifier = verifiersByProvider.get(provider);
        if (verifier == null) {
            throw new UnsupportedProvider(provider.name());
        }

        // 2. verificar el token y normalizar la identidad
        SocialUserInfo info = verifier.verify(token);

        // 3. resolver el usuario (login existente, linking o auto-registro)
        Users user = resolveUser(info);

        // 4. emitir tokens propios, reutilizando el mismo flujo del login local
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    private Users resolveUser(SocialUserInfo info) {
        // 1. usuario recurrente: ya existe la identidad para este proveedor
        var existingIdentity = identityRepo.findByProviderAndProviderUserId(
            info.provider(),
            info.providerUserId()
        );
        if (existingIdentity.isPresent()) {
            return existingIdentity.get().getUser();
        }

        // 2. primer login con este proveedor: buscar cuenta local por email normalizado
        String normalizedEmail = normalizeEmail(info.email());
        var existingUser = userRepo.findByEmail(normalizedEmail);

        if (existingUser.isPresent()) {
            // 3. solo se vincula si el proveedor certifica el email como verificado
            if (!info.emailVerified()) {
                throw new EmailNotVerified(normalizedEmail);
            }
            Users user = existingUser.get();
            linkIdentity(user, info);
            return user;
        }

        // 4. no existe cuenta local: registro automatico, sin password (cuenta solo-social)
        Users newUser = userRepo.save(
            Users.builder()
                .email(normalizedEmail)
                .password(null)
                .role(Role.USER)
                .build()
        );
        linkIdentity(newUser, info);
        return newUser;
    }

    private void linkIdentity(Users user, SocialUserInfo info) {
        identityRepo.save(
            UserIdentity.builder()
                .user(user)
                .provider(info.provider())
                .providerUserId(info.providerUserId())
                .email(info.email())
                .build()
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
