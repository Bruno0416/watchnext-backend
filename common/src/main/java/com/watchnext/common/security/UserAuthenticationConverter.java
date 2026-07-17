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
        String id = jwt.getClaimAsString("id");
        // 1. salir temprano si el token no trae los claims minimos requeridos
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                "El token JWT debe contener el claim 'id' no vacio"
            );
        }

        // 2. extraer el rol del token
        String role = jwt.getClaimAsString("role");

        // 3. construir la lista de authorities a partir del rol
        List<GrantedAuthority> authorities = (role == null || role.isBlank())
            ? List.of()
            : List.of(new SimpleGrantedAuthority("ROLE_" + role));

        // 4. construir el usuario a partir de los claims
        User user = new User(
            id,
            jwt.getClaimAsString("name"),
            jwt.getClaimAsString("email"),
            role
        );

        // 5. retornar el token de autenticacion con el usuario y authorities
        return new UserAuthenticationToken(user, jwt, authorities);
    }
}
