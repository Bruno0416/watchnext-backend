package com.watchnext.common.security.internal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

@Configuration
@ConditionalOnClass(ExchangeFilterFunction.class)
public class ServiceWebClientConfig {

    @Bean
    public ServiceWebClientFilter serviceWebClientFilter(
        ServiceTokenProvider tokenProvider
    ) {
        return new ServiceWebClientFilter(tokenProvider);
    }
}
