package com.watchnext.auth_service.client;

import com.watchnext.common.dto.internal.ConfirmationEmailRequest;
import com.watchnext.common.dto.internal.RecoveryEmailRequest;
import com.watchnext.common.enums.Language;
import com.watchnext.common.security.internal.ServiceRestClientInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EmailServiceClient {

    private final RestClient restClient;

    public EmailServiceClient(
        @Value("${email-service.api.base-url}") String baseUrl,
        ServiceRestClientInterceptor serviceRestClientInterceptor
    ) {
        // 1. adjuntar el interceptor de token de servicio solo a este cliente dedicado
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .requestInterceptor(serviceRestClientInterceptor)
            .build();
    }

    public void sendConfirmation(String to, String code, Language language) {
        // 1. crear request
        ConfirmationEmailRequest request = new ConfirmationEmailRequest(
            to,
            code
        );

        // 2. enviar request al servicio
        restClient
            .post()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/send-confirmation")
                    .queryParam("language", language)
                    .build()
            )
            .body(request)
            .retrieve()
            .toBodilessEntity();
    }

    public void sendPasswordRecovery(
        String to,
        String code,
        Language language
    ) {
        // 1. crear request
        RecoveryEmailRequest request = new RecoveryEmailRequest(to, code);

        // 2. enviar request al servicio
        restClient
            .post()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/send-password-recovery")
                    .queryParam("language", language)
                    .build()
            )
            .body(request)
            .retrieve()
            .toBodilessEntity();
    }
}
