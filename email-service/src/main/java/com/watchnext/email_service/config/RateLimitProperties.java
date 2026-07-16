package com.watchnext.email_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "email.rate-limit")
public record RateLimitProperties(int cooldownSeconds, int maxPerHour) {

    public RateLimitProperties {
        if (cooldownSeconds <= 0) {
            cooldownSeconds = 60;
        }
        if (maxPerHour <= 0) {
            maxPerHour = 5;
        }
    }
}
