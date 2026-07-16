package com.watchnext.email_service.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.watchnext.common.enums.CodeType;
import com.watchnext.common.exceptions.RateLimitExceeded;
import com.watchnext.email_service.config.RateLimitProperties;
import org.junit.jupiter.api.Test;

class EmailRateLimiterTest {

    // Cooldown de 60s, maximo 5 por hora (misma politica que produccion)
    private final RateLimitProperties properties = new RateLimitProperties(60, 5);

    @Test
    void secondCallWithinCooldownIsThrottled() {
        // 1. preparar limitador y disparar el primer envio (permitido)
        EmailRateLimiter limiter = new EmailRateLimiter(properties);
        limiter.checkAndRecord("user@example.com", CodeType.CONFIRMATION);

        // 2. el segundo envio inmediato al mismo destinatario+tipo debe rechazarse
        assertThatThrownBy(() ->
            limiter.checkAndRecord("user@example.com", CodeType.CONFIRMATION)
        )
            .isInstanceOf(RateLimitExceeded.class)
            .satisfies(ex -> {
                RateLimitExceeded rateLimitExceeded = (RateLimitExceeded) ex;
                assertThat(rateLimitExceeded.getRetryAfterSeconds()).isEqualTo(60);
            });
    }

    @Test
    void sixthCallWithinTheHourExceedsQuota() {
        // 1. agotar la cuota horaria evitando el cooldown entre llamadas
        RateLimitProperties noCooldown = new RateLimitProperties(1, 5);
        EmailRateLimiter limiter = new EmailRateLimiter(noCooldown);
        String to = "quota@example.com";

        for (int i = 0; i < 5; i++) {
            limiter.checkAndRecord(to, CodeType.PASSWORD_RECOVERY);
            waitCooldown();
        }

        // 2. el sexto envio dentro de la misma ventana horaria debe rechazarse
        assertThatThrownBy(() ->
            limiter.checkAndRecord(to, CodeType.PASSWORD_RECOVERY)
        )
            .isInstanceOf(RateLimitExceeded.class)
            .satisfies(ex -> {
                RateLimitExceeded rateLimitExceeded = (RateLimitExceeded) ex;
                assertThat(
                    rateLimitExceeded.getRetryAfterSeconds()
                ).isGreaterThan(0);
            });
    }

    @Test
    void differentRecipientOrTypeIsIndependentBucket() {
        // 1. agotar el bucket de un destinatario+tipo especifico
        EmailRateLimiter limiter = new EmailRateLimiter(properties);
        limiter.checkAndRecord("a@example.com", CodeType.CONFIRMATION);

        // 2. otro destinatario, o el mismo destinatario con otro tipo, no esta limitado
        assertThatCode(() ->
            limiter.checkAndRecord("b@example.com", CodeType.CONFIRMATION)
        ).doesNotThrowAnyException();
        assertThatCode(() ->
            limiter.checkAndRecord("a@example.com", CodeType.PASSWORD_RECOVERY)
        ).doesNotThrowAnyException();
    }

    @Test
    void unexpectedFailureInsideLimiterFailsOpen() {
        // 1. propiedades nulas provocan un NPE interno al construir las caches
        assertThatCode(() -> {
            EmailRateLimiter limiter = new EmailRateLimiter(properties);
            // 2. simular un destinatario nulo: no debe propagar la excepcion (fail-open)
            limiter.checkAndRecord(null, CodeType.CONFIRMATION);
        }).doesNotThrowAnyException();
    }

    private void waitCooldown() {
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
