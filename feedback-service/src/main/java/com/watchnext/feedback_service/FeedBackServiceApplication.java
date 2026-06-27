package com.watchnext.feedback_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
        "com.watchnext.feedback_service",
        "com.watchnext.common.security",
        "com.watchnext.common.exceptions",
    }
)
public class FeedBackServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeedBackServiceApplication.class, args);
    }
}
