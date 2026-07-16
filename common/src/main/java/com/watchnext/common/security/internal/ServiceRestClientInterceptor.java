package com.watchnext.common.security.internal;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class ServiceRestClientInterceptor implements ClientHttpRequestInterceptor {

    private final ServiceTokenProvider tokenProvider;

    public ServiceRestClientInterceptor(ServiceTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public ClientHttpResponse intercept(
        HttpRequest request,
        byte[] body,
        ClientHttpRequestExecution execution
    ) throws IOException {
        request.getHeaders().setBearerAuth(tokenProvider.getToken());
        return execution.execute(request, body);
    }
}
