package com.watchnext.email_service.config;

import dev.akkinoc.util.YamlResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class MessageConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource =
            new ResourceBundleMessageSource() {
                @Override
                protected ResourceBundle doGetBundle(
                    String basename,
                    Locale locale
                ) {
                    return ResourceBundle.getBundle(
                        basename,
                        locale,
                        YamlResourceBundle.Control.INSTANCE
                    );
                }
            };

        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
