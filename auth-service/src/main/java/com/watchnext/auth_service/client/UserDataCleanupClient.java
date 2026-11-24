package com.watchnext.auth_service.client;

import com.watchnext.common.security.internal.ServiceRestClientInterceptor;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// borra las listas y reseñas de una cuenta que elimina el admin
@Slf4j
@Component
public class UserDataCleanupClient {

    private final RestClient listClient;
    private final RestClient feedbackClient;

    public UserDataCleanupClient(
        @Value("${list-service.api.base-url}") String listBaseUrl,
        @Value("${feedback-service.api.base-url}") String feedbackBaseUrl,
        ServiceRestClientInterceptor serviceRestClientInterceptor
    ) {
        // 1. clientes con token de servicio para las rutas internas
        this.listClient = RestClient.builder()
            .baseUrl(listBaseUrl)
            .requestInterceptor(serviceRestClientInterceptor)
            .build();
        this.feedbackClient = RestClient.builder()
            .baseUrl(feedbackBaseUrl)
            .requestInterceptor(serviceRestClientInterceptor)
            .build();
    }

    public void deleteUserData(UUID authUserId) {
        // 1. si un servicio no responde la baja sigue, solo queda en el log
        delete(listClient, authUserId, "list-service");
        delete(feedbackClient, authUserId, "feedback-service");
    }

    private void delete(RestClient client, UUID authUserId, String service) {
        try {
            client.delete().uri("/users/{id}", authUserId).retrieve().toBodilessEntity();
        } catch (RestClientException ex) {
            log.warn("No se pudieron borrar los datos de {} en {}: {}", authUserId, service, ex.getMessage());
        }
    }
}
