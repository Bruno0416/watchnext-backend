package com.watchnext.list_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
        "com.watchnext.list_service",
        "com.watchnext.common.exceptions",
        "com.watchnext.common.security",
    }
)
public class ListServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ListServiceApplication.class, args);
    }
}
