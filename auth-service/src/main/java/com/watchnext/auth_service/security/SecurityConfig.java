package com.watchnext.auth_service.security;

import com.watchnext.common.security.UserAuthenticationConverter;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;

    @Value("${application.security.jwt.secret-key}")
    private String jwtSecret;

    // ---------- panel admin: token valido y rol ADMIN ----------
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http)
        throws Exception {
        http.securityMatcher("/api/v1/auth/admin/**")
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("ADMIN"))
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.decoder(adminJwtDecoder())
                        .jwtAuthenticationConverter(new UserAuthenticationConverter())
                )
            );

        return http.build();
    }

    // ---------- login, registro y demas: publicos ----------
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .authorizeHttpRequests(auth ->
                auth
                    .requestMatchers("/api/v1/auth/**")
                    .permitAll()
                    .anyRequest()
                    .denyAll()
            );

        return http.build();
    }

    // mismo secreto y algoritmo con que JwtUtil firma los tokens
    private JwtDecoder adminJwtDecoder() {
        SecretKey key = new SecretKeySpec(
            jwtSecret.getBytes(StandardCharsets.UTF_8),
            "HmacSHA512"
        );
        return NimbusJwtDecoder.withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS512)
            .build();
    }
}
