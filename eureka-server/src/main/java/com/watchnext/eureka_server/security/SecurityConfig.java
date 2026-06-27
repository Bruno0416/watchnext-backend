package com.watchnext.eureka_server.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. FILTRO PARA MICROSERVICIOS
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http)
        throws Exception {
        http.securityMatcher("/eureka/apps/**")
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // 2. FILTRO PARA WEB (panel de eureka)
    @Bean
    @Order(2)
    public SecurityFilterChain uiFilterChain(HttpSecurity http)
        throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth ->
                auth
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            )
            .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
