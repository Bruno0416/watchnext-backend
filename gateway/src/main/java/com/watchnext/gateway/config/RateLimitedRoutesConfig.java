package com.watchnext.gateway.config;

import static org.springframework.cloud.gateway.server.mvc.filter.Bucket4jFilterFunctions.rateLimit;
import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class RateLimitedRoutesConfig {

    // 1. AUTH
    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return route("auth-service")
            .route(path("/api/v1/auth/**"), http())
            .filter(lb("AUTH-SERVICE"))
            .filter(
                rateLimit(c ->
                    c
                        .setCapacity(5) // 5 peticiones
                        .setPeriod(Duration.ofMinutes(1)) // por minuto
                        .setKeyResolver(
                            RateLimitedRoutesConfig::resolveClientIp
                        )
                )
            )
            .build();
    }

    // 2. CONTENT
    @Bean
    public RouterFunction<ServerResponse> contentServiceRoute() {
        return route("content-service")
            .route(path("/api/v1/content/**"), http())
            .filter(lb("CONTENT-SERVICE"))
            .filter(
                rateLimit(c ->
                    c
                        .setCapacity(60) // 60 peticiones
                        .setPeriod(Duration.ofMinutes(1)) // por minuto
                        .setKeyResolver(
                            RateLimitedRoutesConfig::resolveClientIp
                        )
                )
            )
            .build();
    }

    // 3. LIST
    @Bean
    public RouterFunction<ServerResponse> listServiceRoute() {
        return route("list-service")
            .route(path("/api/v1/list/**"), http())
            .filter(lb("LIST-SERVICE"))
            .filter(
                rateLimit(c ->
                    c
                        .setCapacity(30)
                        .setPeriod(Duration.ofMinutes(1))
                        .setKeyResolver(
                            RateLimitedRoutesConfig::resolveClientIp
                        )
                )
            )
            .build();
    }

    // 4. USER
    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return route("user-service")
            .route(path("/api/v1/users/**"), http())
            .filter(lb("USER-SERVICE"))
            .filter(
                rateLimit(c ->
                    c
                        .setCapacity(15)
                        .setPeriod(Duration.ofMinutes(1))
                        .setKeyResolver(
                            RateLimitedRoutesConfig::resolveClientIp
                        )
                )
            )
            .build();
    }

    // 5. FEEDBACK
    @Bean
    public RouterFunction<ServerResponse> feedbackServiceRoute() {
        return route("feedback-service")
            .route(path("/api/v1/feedback/**"), http())
            .filter(lb("FEEDBACK-SERVICE"))
            .filter(
                rateLimit(c ->
                    c
                        .setCapacity(10)
                        .setPeriod(Duration.ofMinutes(1))
                        .setKeyResolver(
                            RateLimitedRoutesConfig::resolveClientIp
                        )
                )
            )
            .build();
    }

    // Extraccion de IP para asociar el Bucket a cada cliente
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
}
