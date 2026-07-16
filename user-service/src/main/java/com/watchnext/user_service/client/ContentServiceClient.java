package com.watchnext.user_service.client;

import com.watchnext.common.dto.ContentRefRequest;
import com.watchnext.common.dto.internal.ContentBasicDetail;
import com.watchnext.common.security.internal.ServiceRestClientInterceptor;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ContentServiceClient {

    private final RestClient restClient;

    // ---------- configuracion ----------
    public ContentServiceClient(
        @Value("${content-service.api.base-url}") String baseUrl,
        ServiceRestClientInterceptor serviceRestClientInterceptor
    ) {
        // 1. adjuntar el interceptor de token de servicio solo a este cliente dedicado
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .requestInterceptor(serviceRestClientInterceptor)
            .build();
    }

    // ---------- consumo api externa ----------
    public List<ContentBasicDetail> fetchBulkContent(
        List<ContentRefRequest> requests,
        String language
    ) {
        // 1. ejecutar peticion post para obtener multiples contenidos de forma masiva
        return restClient
            .post()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/bulk")
                    .queryParam("language", language)
                    .build()
            )
            // 2. enviar lista de referencias en el cuerpo de la peticion
            .body(requests)
            .retrieve()
            // 3. parsear y retornar la respuesta como una lista de detalles basicos
            .body(
                new ParameterizedTypeReference<List<ContentBasicDetail>>() {}
            );
    }
}
