package com.watchnext.auth_service.service.code;

import com.watchnext.auth_service.entity.VerificationCode;
import com.watchnext.auth_service.repository.VerificationCodeRepository;
import com.watchnext.auth_service.utils.CodeGeneratorUtil;
import com.watchnext.common.enums.CodeType;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;

    @Value("${verification.code.expiration-minutes}")
    private long expirationMinutes;

    @Transactional
    @Override
    public String generateCode(String email, CodeType type) {
        // 1. invalidar cualquier codigo previo sin usar de este email + type
        verificationCodeRepository.deleteAllByEmailAndType(email, type);

        // 2. generar y persistir el nuevo
        String code = CodeGeneratorUtil.generateCode();

        VerificationCode verificationCode = VerificationCode.builder()
            .email(email)
            .code(code)
            .type(type)
            .expiresAt(
                Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)
            )
            .build();

        verificationCodeRepository.save(verificationCode);
        return code;
    }

    @Transactional
    @Override
    public boolean validateCode(
        String email,
        CodeType type,
        String submittedCode
    ) {
        String normalized =
            submittedCode == null ? "" : submittedCode.toUpperCase();

        return verificationCodeRepository
            .findByEmailAndTypeAndCodeAndUsedFalse(email, type, normalized)
            .filter(vc -> !vc.isExpired())
            .map(vc -> {
                vc.setUsed(true);
                verificationCodeRepository.save(vc);
                return true;
            })
            .orElse(false);
    }
}
