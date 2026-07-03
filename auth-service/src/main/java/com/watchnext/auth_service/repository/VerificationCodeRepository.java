package com.watchnext.auth_service.repository;

import com.watchnext.auth_service.entity.VerificationCode;
import com.watchnext.common.enums.CodeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository
    extends JpaRepository<VerificationCode, UUID>
{
    Optional<VerificationCode> findByEmailAndTypeAndCodeAndUsedFalse(
        String email,
        CodeType type,
        String code
    );

    void deleteAllByEmailAndType(String email, CodeType type);
}
