package com.watchnext.common.security.internal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class ServiceTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String SERVICE_TOKEN_TYPE = "SERVICE";

    private final SecretKey key;

    public ServiceTokenAuthenticationFilter(ServiceTokenProperties props) {
        this.key = new SecretKeySpec(
            props.getSecretKey().getBytes(StandardCharsets.UTF_8),
            "HmacSHA512"
        );
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        // 1. extraer el header de autorizacion
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Unauthorized\",\"message\":\"Missing or invalid Authorization header\"}"
            );
            response.flushBuffer();
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 2. validar firma y extraer claims
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            // 3. verificar que sea un token de servicio (no de usuario)
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            if (!SERVICE_TOKEN_TYPE.equals(tokenType)) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\":\"Forbidden\",\"message\":\"Invalid token type for internal endpoint\"}"
                );
                response.flushBuffer();
                return;
            }

            // 4. verificar que no haya expirado
            if (claims.getExpiration().before(new Date())) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            // 5. establecer autenticacion en el contexto como servicio
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_SERVICE"));
            var authentication = new UsernamePasswordAuthenticationToken(
                claims.getSubject(),
                null,
                authorities
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // 6. token invalido o manipulado
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Unauthorized\",\"message\":\"Invalid service token signature or format\"}"
            );
            response.flushBuffer();
        }
    }
}
