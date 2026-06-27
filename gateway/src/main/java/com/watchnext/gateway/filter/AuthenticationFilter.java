package com.watchnext.gateway.filter;

import com.watchnext.gateway.exceptions.*;
import com.watchnext.gateway.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public AuthenticationFilter(
        JwtUtil jwtUtil,
        HandlerExceptionResolver handlerExceptionResolver
    ) {
        this.jwtUtil = jwtUtil;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();

        // 1. definir rutas publicas
        List<String> publicPaths = List.of("/api/v1/auth", "/api/v1/content");

        // 2. validar si la ruta empieza con alguna de las rutas publicas
        boolean isPublicPath = publicPaths
            .stream()
            .anyMatch(publicPath -> path.startsWith(publicPath));

        if (!isPublicPath) {
            try {
                String token;
                String authHeader = request.getHeader(
                    HttpHeaders.AUTHORIZATION
                );

                // 1. Verificación contra nulos antes de chequear si está vacío
                if (authHeader == null || authHeader.isEmpty()) {
                    throw new MissingHeaderException(
                        "Falta el header de autorización"
                    );
                }

                if (authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                } else {
                    throw new TokenFormatInvalidException(
                        "Formato de token inválido"
                    );
                }

                // 2. Validar token
                jwtUtil.validateToken(token);
            } catch (ExpiredJwtException e) {
                handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new TokenExpiredException("El token ha expirado")
                );
                return;
            } catch (MissingHeaderException | TokenFormatInvalidException e) {
                handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    e
                );
                return;
            } catch (Exception e) {
                handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new TokenInvalidException(
                        "El token es inválido o manipulado"
                    )
                );
                return;
            }
        }

        // Si es una ruta de auth o si trae un token valido lo dejamos pasar al Gateway
        filterChain.doFilter(request, response);
    }
}
