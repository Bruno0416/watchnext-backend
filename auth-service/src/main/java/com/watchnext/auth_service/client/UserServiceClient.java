package com.watchnext.auth_service.client;

import com.watchnext.auth_service.dto.admin.AdminIdentityRequest;
import com.watchnext.auth_service.dto.admin.AdminProfileData;
import com.watchnext.common.exceptions.WatchNextException;
import com.watchnext.common.security.internal.ServiceRestClientInterceptor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

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

    // ---------- panel admin ----------
    public List<AdminProfileData> getAdminProfiles(List<UUID> authUserIds) {
        // 1. pedir los perfiles de varias cuentas en una sola llamada
        try {
            AdminProfileData[] profiles = restClient
                .post()
                .uri("/admin/profiles")
                .body(authUserIds.stream().map(UUID::toString).toList())
                .retrieve()
                .body(AdminProfileData[].class);
            return profiles == null ? List.of() : Arrays.asList(profiles);
        } catch (RestClientException ex) {
            // 2. sin user-service se muestran las cuentas sin datos de perfil
            log.warn("No se pudieron leer los perfiles en user-service: {}", ex.getMessage());
            return List.of();
        }
    }

    public AdminProfileData updateIdentity(UUID authUserId, AdminIdentityRequest request) {
        try {
            return restClient
                .put()
                .uri("/admin/{id}/identity", authUserId)
                .body(request)
                .retrieve()
                .body(AdminProfileData.class);
        } catch (RestClientException ex) {
            throw translate(ex);
        }
    }

    public void deleteProfile(UUID authUserId) {
        try {
            restClient
                .delete()
                .uri("/admin/{id}", authUserId)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientException ex) {
            throw translate(ex);
        }
    }

    // ---------- helpers ----------
    private WatchNextException translate(RestClientException ex) {
        // 1. mantener el status y el mensaje que devolvio user-service
        if (ex instanceof RestClientResponseException response) {
            String detail = "No se pudo completar la operacion en user-service";
            try {
                Map<?, ?> body = response.getResponseBodyAs(Map.class);
                if (body != null && body.get("detail") instanceof String text) detail = text;
            } catch (RuntimeException ignored) {
                // cuerpo vacio o no json
            }
            return new WatchNextException(HttpStatus.valueOf(response.getStatusCode().value()), detail);
        }
        // 2. user-service caido
        return new WatchNextException(HttpStatus.SERVICE_UNAVAILABLE, "user-service no esta disponible");
    }
}
