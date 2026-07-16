package com.watchnext.search_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
        "com.watchnext.search_service",
        "com.watchnext.common.exceptions",
        "com.watchnext.common.security",
        "com.watchnext.common.security.internal",
        "com.watchnext.common.config"
    }
)
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
