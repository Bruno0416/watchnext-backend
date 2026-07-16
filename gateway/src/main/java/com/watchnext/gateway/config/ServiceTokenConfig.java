package com.watchnext.gateway.config;

import com.watchnext.common.security.internal.ServiceRestClientInterceptor;
import com.watchnext.common.security.internal.ServiceTokenProperties;
import com.watchnext.common.security.internal.ServiceTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ServiceTokenProperties.class)
public class ServiceTokenConfig {

    @Bean
    public ServiceTokenProvider serviceTokenProvider(
        ServiceTokenProperties props,
        @Value("${spring.application.name:gateway}") String serviceName
    ) {
        return new ServiceTokenProvider(props, serviceName);
    }

    @Bean
    public ServiceRestClientInterceptor serviceRestClientInterceptor(
        ServiceTokenProvider tokenProvider
    ) {
        return new ServiceRestClientInterceptor(tokenProvider);
    }
}
