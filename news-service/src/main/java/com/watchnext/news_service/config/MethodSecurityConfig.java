package com.watchnext.news_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// habilita @PreAuthorize para que solo ADMIN publique noticias
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {}
