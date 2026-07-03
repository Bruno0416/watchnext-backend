package com.watchnext.user_service.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    Cloudinary cloudinary(@Value("${cloudinary.url}") String url) {
        return new Cloudinary(url);
    }
}
