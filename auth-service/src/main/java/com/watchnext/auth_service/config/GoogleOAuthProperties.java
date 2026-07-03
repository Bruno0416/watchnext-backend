package com.watchnext.auth_service.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth.oauth.google")
public class GoogleOAuthProperties {

    private List<String> allowedAudiences;

    public List<String> getAllowedAudiences() {
        return allowedAudiences;
    }

    public void setAllowedAudiences(List<String> allowedAudiences) {
        this.allowedAudiences = allowedAudiences;
    }
}
