package com.watchnext.email_service.controller;

import com.watchnext.common.dto.internal.ConfirmationEmailRequest;
import com.watchnext.common.dto.internal.RecoveryEmailRequest;
import com.watchnext.common.enums.CodeType;
import com.watchnext.common.enums.Language;
import com.watchnext.email_service.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/email/internal")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-confirmation")
    public ResponseEntity<Void> sendConfirmation(
        @RequestParam(defaultValue = "ES") Language language,
        @Valid @RequestBody ConfirmationEmailRequest request
    ) {
        emailService.buildAndSendEmail(
            request.to(),
            request.code(),
            language,
            CodeType.CONFIRMATION
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-password-recovery")
    public ResponseEntity<Void> sendPasswordRecovery(
        @RequestParam(defaultValue = "ES") Language language,
        @Valid @RequestBody RecoveryEmailRequest request
    ) {
        emailService.buildAndSendEmail(
            request.to(),
            request.code(),
            language,
            CodeType.PASSWORD_RECOVERY
        );
        return ResponseEntity.ok().build();
    }
}
