package com.watchnext.feedback_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class FeedbackSecurityConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain feedbackPublicFilterChain(HttpSecurity http)
        throws Exception {
        // 1. permitir acceso publico al endpoint de reviews agregadas
        http.securityMatcher(
            "/api/v1/feedback/reviews/MOVIE/*",
            "/api/v1/feedback/reviews/TV/*"
        )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}