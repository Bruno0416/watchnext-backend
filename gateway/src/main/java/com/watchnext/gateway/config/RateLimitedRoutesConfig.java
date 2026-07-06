package com.watchnext.gateway.config;

import static org.springframework.cloud.gateway.server.mvc.filter.Bucket4jFilterFunctions.rateLimit;
import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class RateLimitedRoutesConfig {

    // 1. AUTH — endpoints sensibles
    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return route("auth-service")
            .route(path("/api/v1/auth/**"), http())
            .filter(lb("AUTH-SERVICE"))
            .filter(perClientRateLimit(5))
            .build();
    }

    // 2. CONTENT — lecturas cacheadas (Redis 24h)
    @Bean
    public RouterFunction<ServerResponse> contentServiceRoute() {
        return route("content-service")
            .route(path("/api/v1/content/**"), http())
            .filter(lb("CONTENT-SERVICE"))
            .filter(perPathRateLimit(200))
            .build();
    }

    // 3. LIST — datos de usuario en DB
    @Bean
    public RouterFunction<ServerResponse> listServiceRoute() {
        return route("list-service")
            .route(path("/api/v1/list/**"), http())
            .filter(lb("LIST-SERVICE"))
            .filter(perPathRateLimit(60))
            .build();
    }

    // 4. USER — /users/me + favoritos + follows
    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return route("user-service")
            .route(path("/api/v1/users/**"), http())
            .filter(lb("USER-SERVICE"))
            .filter(perPathRateLimit(40))
            .build();
    }

    // 5. FEEDBACK — reseñas/ratings
    @Bean
    public RouterFunction<ServerResponse> feedbackServiceRoute() {
        return route("feedback-service")
            .route(path("/api/v1/feedback/**"), http())
            .filter(lb("FEEDBACK-SERVICE"))
            .filter(perPathRateLimit(20))
            .build();
    }

    // --- Filtros reutilizables ---
    private static HandlerFilterFunction<
        ServerResponse,
        ServerResponse
    > perClientRateLimit(int capacityPerMinute) {
        return rateLimit(c ->
            c
                .setCapacity(capacityPerMinute)
                .setPeriod(Duration.ofMinutes(1))
                .setKeyResolver(RateLimitedRoutesConfig::resolveClientIp)
        );
    }

    private static HandlerFilterFunction<
        ServerResponse,
        ServerResponse
    > perPathRateLimit(int capacityPerMinute) {
        return rateLimit(c ->
            c
                .setCapacity(capacityPerMinute)
                .setPeriod(Duration.ofMinutes(1))
                .setKeyResolver(RateLimitedRoutesConfig::resolveKeyPerPath)
        );
    }

    // --- Key resolvers ---
    private static String resolveClientIp(ServerRequest request) {
        String xff = request.headers().firstHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request
            .remoteAddress()
            .map(addr -> addr.getAddress().getHostAddress())
            .orElse("unknown");
    }

    private static String resolveKeyPerPath(ServerRequest request) {
        return resolveClientIp(request) + "|" + request.uri().getPath();
    }
}
