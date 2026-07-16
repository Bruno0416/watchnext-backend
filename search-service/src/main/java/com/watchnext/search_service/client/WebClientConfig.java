package com.watchnext.search_service.client;

import com.watchnext.common.security.internal.ServiceWebClientFilter;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // ---------- configuracion ----------
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder(
        ServiceWebClientFilter serviceTokenFilter
    ) {
        // 1. crear builder inyectando filtro de seguridad y balanceo de carga
        return WebClient.builder().filter(serviceTokenFilter);
    }
}
