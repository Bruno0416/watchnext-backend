package com.watchnext.auth_service.config;

import com.watchnext.auth_service.enums.AuthProvider;
import com.watchnext.auth_service.exceptions.UnsupportedProvider;
import jakarta.validation.constraints.NotNull;
import java.util.Locale;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AuthProviderConverter implements Converter<String, AuthProvider> {

    @Override
    public AuthProvider convert(@NotNull String source) {
        try {
            return AuthProvider.valueOf(source.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedProvider(source);
        }
    }
}
