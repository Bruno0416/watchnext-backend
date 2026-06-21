package com.watchnext.common.security;

import com.watchnext.common.model.User;
import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class UserAuthenticationToken extends AbstractAuthenticationToken {

    private final User principal;
    private final Jwt credentials;

    public UserAuthenticationToken(
        User principal,
        Jwt credentials,
        Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public User getPrincipal() {
        return principal;
    }

    @Override
    public Jwt getCredentials() {
        return credentials;
    }
}
