package com.watchnext.common.security.internal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnClass(RestClient.class)
public class ServiceRestClientConfig {

    @Bean
    public ServiceRestClientInterceptor serviceRestClientInterceptor(
        ServiceTokenProvider tokenProvider
    ) {
        return new ServiceRestClientInterceptor(tokenProvider);
    }
}
