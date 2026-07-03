package com.watchnext.email_service.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.watchnext.common.enums.CodeType;
import com.watchnext.common.enums.Language;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final Resend resendClient;
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;

    @Value("${resend.mail.from}")
    private String fromAddress;

    public EmailServiceImpl(
        Resend resendClient,
        TemplateEngine templateEngine,
        MessageSource messageSource
    ) {
        this.resendClient = resendClient;
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
    }

    @Override
    public void buildAndSendEmail(
        String to,
        String code,
        Language language,
        CodeType type
    ) {
        // 1. Configurar el idioma (Locale)
        Locale locale =
            language == Language.EN ? Locale.ENGLISH : Locale.of("es");
        // 2. Preparar las variables para la plantilla HTML
        Context context = new Context(locale);
        context.setVariable("code", code);
        context.setVariable("codeType", type);
        // 3. Compilar el HTML final
        String htmlContent = templateEngine.process("base-email", context);

        // 4. Obtener el asunto
        String subjectKey =
            type == CodeType.CONFIRMATION
                ? "email.confirmation.title"
                : "email.recovery.title";
        String subject = messageSource.getMessage(subjectKey, null, locale);
        // 5. Enviar el correo
        sendEmail(to, subject, htmlContent);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromAddress)
                .to(to)
                .subject(subject)
                .html(htmlContent)
                .build();

            CreateEmailResponse data = resendClient.emails().send(params);
            log.info(
                "Correo enviado exitosamente a {}. ID de Resend: {}",
                to,
                data.getId()
            );
        } catch (ResendException e) {
            log.error(
                "Error crítico al enviar correo a {} a través de Resend: {}",
                to,
                e.getMessage(),
                e
            );
            throw new RuntimeException(
                "Fallo al enviar la notificación por correo.",
                e
            );
        }
    }
}
