package com.watchnext.email_service;

import com.watchnext.email_service.config.RateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(
    scanBasePackages = {
        "com.watchnext.email_service",
        "com.watchnext.common.exceptions",
        "com.watchnext.common.security",
    }
)
@EnableConfigurationProperties(RateLimitProperties.class)
public class EmailServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailServiceApplication.class, args);
    }
}
