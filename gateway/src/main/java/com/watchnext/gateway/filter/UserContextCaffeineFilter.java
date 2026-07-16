/*
    NOTA:
        este filtro permite extraer el token, obtener y cachear el pais del usuario
        con un endpoint interno en user-service y lo inyecta en los headers de la peticion
        para los endpoints que lo requieren como en content-service
*/


package com.watchnext.gateway.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.watchnext.common.context.UserContext;
import com.watchnext.common.security.GatewayHeaders;
import com.watchnext.common.security.internal.ServiceRestClientInterceptor;
import com.watchnext.gateway.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Slf4j
@Component
public class UserContextCaffeineFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ServiceRestClientInterceptor serviceInterceptor;
    private final String userServiceBaseUrl;
    private RestClient restClient;

    private final Cache<String, UserContext> userContextCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(30))
            .maximumSize(10_000)
            .build();

    public UserContextCaffeineFilter(
        JwtUtil jwtUtil,
        ServiceRestClientInterceptor serviceInterceptor,
        @Value("${user-service.api.base-url}") String userServiceBaseUrl
    ) {
        this.jwtUtil = jwtUtil;
        this.serviceInterceptor = serviceInterceptor;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @PostConstruct
    public void init() {
        this.restClient = RestClient.builder()
            .baseUrl(userServiceBaseUrl)
            .requestInterceptor(serviceInterceptor)
            .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/eureka");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String requestPath = request.getRequestURI();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("[GATEWAY] No JWT found for path={}, using default context (US)", requestPath);
            filterChain.doFilter(wrapRequest(request, UserContext.defaultContext()), response);
            return;
        }

        String token = authHeader.substring(7);
        String userId = null;
        try {
            userId = jwtUtil.extractUserId(token);
            log.info("[GATEWAY] Extracted userId={} for path={}", userId, requestPath);
        } catch (Exception e) {
            log.warn("[GATEWAY] Failed to extract userId from token for path={}", requestPath, e);
        }

        UserContext context = null;
        if (userId != null && !userId.isEmpty()) {
             context = userContextCache.get(userId, this::fetchContext);
             if (context != null) {
                 log.info("[GATEWAY] Fetched context for userId={}: country={}, region={}", userId, context.country(), context.region());
             } else {
                 log.warn("[GATEWAY] Failed to fetch context for userId={}, will use default", userId);
             }
        }

        UserContext finalContext = context != null ? context : UserContext.defaultContext();
        log.info("[GATEWAY] Injecting headers for path={}: User-Country={}, Region={}",
                requestPath, finalContext.country(), finalContext.region());

        filterChain.doFilter(wrapRequest(request, finalContext), response);
    }

    private UserContext fetchContext(String userId) {
        try {
            return restClient.get().uri("/{id}/context", userId).retrieve().body(UserContext.class);
        } catch (Exception e) {
            log.warn("[GATEWAY] fetchContext failed for userId={}: {}", userId, e.getMessage());
            return null;
        }
    }

    private HttpServletRequest wrapRequest(HttpServletRequest request, UserContext ctx) {
        log.info("[GATEWAY] Wrapping request with context: country={}, region={}", ctx.country(), ctx.region());

        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (GatewayHeaders.COUNTRY.equalsIgnoreCase(name)) {
                    String value = ctx.country();
                    log.info("[GATEWAY] getHeader({}) = {} (INJECTED)", name, value);
                    return value;
                }
                if (GatewayHeaders.REGION.equalsIgnoreCase(name)) {
                    String value = ctx.region();
                    log.info("[GATEWAY] getHeader({}) = {} (INJECTED)", name, value);
                    return value;
                }
                String originalValue = super.getHeader(name);
                log.debug("[GATEWAY] getHeader({}) = {} (original)", name, originalValue);
                return originalValue;
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (GatewayHeaders.COUNTRY.equalsIgnoreCase(name)) {
                    log.info("[GATEWAY] getHeaders({}) = [{}] (INJECTED)", name, ctx.country());
                    return Collections.enumeration(Collections.singletonList(ctx.country()));
                }
                if (GatewayHeaders.REGION.equalsIgnoreCase(name)) {
                    log.info("[GATEWAY] getHeaders({}) = [{}] (INJECTED)", name, ctx.region());
                    return Collections.enumeration(Collections.singletonList(ctx.region()));
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> names = Collections.list(super.getHeaderNames());
                if (ctx.country() != null) {
                    names.add(GatewayHeaders.COUNTRY);
                    log.info("[GATEWAY] Added {} to header names", GatewayHeaders.COUNTRY);
                }
                if (ctx.region() != null) {
                    names.add(GatewayHeaders.REGION);
                    log.info("[GATEWAY] Added {} to header names", GatewayHeaders.REGION);
                }
                return Collections.enumeration(names);
            }
        };
    }
}
