package com.watchnext.auth_service.service.code;

import com.watchnext.common.enums.CodeType;

public interface VerificationCodeService {
    String generateCode(String email, CodeType type);
    boolean validateCode(String email, CodeType type, String submittedCode);
}
