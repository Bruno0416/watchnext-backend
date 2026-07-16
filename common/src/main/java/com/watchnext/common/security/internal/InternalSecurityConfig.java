package com.watchnext.common.security.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableConfigurationProperties(ServiceTokenProperties.class)
public class InternalSecurityConfig {

    @Bean
    @Order(-10)
    public SecurityFilterChain internalFilterChain(
        HttpSecurity http,
        ServiceTokenAuthenticationFilter filter
    ) throws Exception {
        // 1. proteger rutas internas de todos los servicios
        http.securityMatcher(
                PathPatternRequestMatcher.pathPattern("/api/v1/*/internal/**")
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public ServiceTokenAuthenticationFilter serviceTokenAuthenticationFilter(
        ServiceTokenProperties props
    ) {
        return new ServiceTokenAuthenticationFilter(props);
    }

    // 1. evitar que Spring Boot registre este filtro como filtro de servlet global (todas las rutas)
    // 2. Spring Security ya lo añade explicitamente al chain interno via addFilterBefore
    @Bean
    public FilterRegistrationBean<ServiceTokenAuthenticationFilter> serviceTokenAuthenticationFilterRegistration(
        ServiceTokenAuthenticationFilter filter
    ) {
        FilterRegistrationBean<ServiceTokenAuthenticationFilter> registration =
            new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public ServiceTokenProvider serviceTokenProvider(
        ServiceTokenProperties props,
        @Value("${spring.application.name:unknown-service}") String serviceName
    ) {
        return new ServiceTokenProvider(props, serviceName);
    }
}
