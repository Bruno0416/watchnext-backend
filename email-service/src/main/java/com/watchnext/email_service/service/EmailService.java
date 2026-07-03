package com.watchnext.email_service.service;

import com.watchnext.common.enums.CodeType;
import com.watchnext.common.enums.Language;

public interface EmailService {
    void buildAndSendEmail(
        String to,
        String code,
        Language language,
        CodeType type
    );
}
