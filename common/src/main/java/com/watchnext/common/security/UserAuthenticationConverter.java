package com.watchnext.common.security;

import com.watchnext.common.model.User;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class UserAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken>
{

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String role = jwt.getClaimAsString("role");

        List<GrantedAuthority> authorities = (role == null || role.isBlank())
            ? List.of()
            : List.of(new SimpleGrantedAuthority("ROLE_" + role));

        User user = new User(
            jwt.getClaimAsString("id"),
            jwt.getClaimAsString("name"),
            jwt.getClaimAsString("email"),
            role
        );

        return new UserAuthenticationToken(user, jwt, authorities);
    }
}
