package com.watchnext.auth_service.security.social;

import com.watchnext.auth_service.dto.social.SocialUserInfo;
import com.watchnext.auth_service.enums.AuthProvider;

public interface SocialTokenVerifier {
    // 1. proveedor que maneja esta estrategia (clave del map en SocialAuthService)
    AuthProvider provider();

    // 2. verifica el token del proveedor y devuelve la identidad normalizada.
    SocialUserInfo verify(String token);
}
