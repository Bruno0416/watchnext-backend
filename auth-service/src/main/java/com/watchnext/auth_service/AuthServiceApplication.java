package com.watchnext.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
        "com.watchnext.auth_service",
        "com.watchnext.common.exceptions",
        "com.watchnext.common.config",
        "com.watchnext.common.security.internal"
    }
)
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
