package com.watchnext.common.security.internal;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.security.service-token")
public class ServiceTokenProperties {

    private String secretKey;
    private Duration expiration = Duration.ofMinutes(3);
    private Duration cacheTtl = Duration.ofSeconds(150);

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Duration getExpiration() {
        return expiration;
    }

    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }

    public Duration getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(Duration cacheTtl) {
        this.cacheTtl = cacheTtl;
    }
}
