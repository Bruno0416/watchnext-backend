package com.watchnext.auth_service.client;

import com.watchnext.common.security.internal.ServiceRestClientInterceptor;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(
        @Value("${user-service.api.base-url}") String baseUrl,
        ServiceRestClientInterceptor serviceRestClientInterceptor
    ) {
        // 1. cliente dedicado con token de servicio para los endpoints internos
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .requestInterceptor(serviceRestClientInterceptor)
            .build();
    }

    public void provisionProfile(UUID authUserId) {
        // 1. pedir a user-service que cree el perfil vacio del usuario nuevo
        try {
            restClient
                .post()
                .uri("/provision")
                .body(Map.of("authUserId", authUserId))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientException ex) {
            // 2. si user-service no responde, el perfil se crea igual en el onboarding
            log.warn("No se pudo crear el perfil de {} en user-service: {}", authUserId, ex.getMessage());
        }
    }
}
