package com.watchnext.email_service.config;

import com.watchnext.common.enums.Language;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LanguageConverter implements Converter<String, Language> {

    @Override
    public Language convert(String source) {
        return Language.fromString(source);
    }
}
