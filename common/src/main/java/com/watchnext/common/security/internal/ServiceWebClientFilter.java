package com.watchnext.common.security.internal;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

public class ServiceWebClientFilter implements ExchangeFilterFunction {

    private final ServiceTokenProvider tokenProvider;

    public ServiceWebClientFilter(ServiceTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        // 1. adjuntar token de servicio solo si la peticion no trae ya una credencial
        if (request.headers().getFirst(HttpHeaders.AUTHORIZATION) == null) {
            ClientRequest mutated = ClientRequest.from(request)
                .headers(headers -> headers.setBearerAuth(tokenProvider.getToken()))
                .build();
            return next.exchange(mutated);
        }
        return next.exchange(request);
    }
}
