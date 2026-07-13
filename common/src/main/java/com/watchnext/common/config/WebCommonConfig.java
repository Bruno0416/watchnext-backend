package com.watchnext.common.config;

import com.watchnext.common.config.converters.StringToEnumConverterFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebCommonConfig {

    @Configuration
    @RequiredArgsConstructor
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public static class WebFluxConfig implements WebFluxConfigurer {


        private final StringToEnumConverterFactory converterFactory;

        @Override
        public void addFormatters(FormatterRegistry registry) {
            registry.addConverterFactory(this.converterFactory);
        }
    }

    @Configuration
    @RequiredArgsConstructor
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class WebMvcConfig implements WebMvcConfigurer {


        private final StringToEnumConverterFactory converterFactory;

        @Override
        public void addFormatters(FormatterRegistry registry) {
            registry.addConverterFactory(this.converterFactory);
        }
    }
}
