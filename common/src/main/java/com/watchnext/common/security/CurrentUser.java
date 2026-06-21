package com.watchnext.common.security;

import com.watchnext.common.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {

    private CurrentUser() {}

    public static User get() {
        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new IllegalStateException(
                "No hay usuario autenticado en el contexto"
            );
        }
        return user;
    }

    public static String id() {
        return get().id();
    }
}
