package com.watchnext.email_service.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.watchnext.common.enums.CodeType;
import com.watchnext.common.exceptions.RateLimitExceeded;
import com.watchnext.email_service.config.RateLimitProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// Limitador de envios por destinatario + tipo de codigo, respaldado por caches
// en memoria (Caffeine). Unica instancia de email-service, sin necesidad de Redis.
@Slf4j
@Component
public class EmailRateLimiter {

    private final RateLimitProperties properties;
    private final Cache<String, Boolean> cooldownCache;
    private final Cache<String, HourlyWindow> hourlyCache;

    public EmailRateLimiter(RateLimitProperties properties) {
        this.properties = properties;
        this.cooldownCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(properties.cooldownSeconds()))
            .build();
        this.hourlyCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofHours(1))
            .build();
    }

    // Verifica si el envio esta permitido y, de ser asi, lo registra.
    // Falla abierto: cualquier error inesperado en el limitador permite el envio.
    public void checkAndRecord(String to, CodeType type) {
        try {
            // 1. calcular la clave de agrupacion por tipo de codigo + destinatario
            String key = type.name() + ":" + to;

            // 2. rechazar si todavia esta en cooldown desde el ultimo envio
            if (cooldownCache.getIfPresent(key) != null) {
                throw new RateLimitExceeded(
                    "Too many requests, please wait before requesting another code",
                    properties.cooldownSeconds()
                );
            }

            // 3. rechazar si ya se alcanzo la cuota maxima de la ventana horaria
            HourlyWindow window = hourlyCache.get(key, k -> new HourlyWindow());
            int retryAfterSeconds = window.retryAfterSecondsIfLimitReached(
                properties.maxPerHour()
            );
            if (retryAfterSeconds > 0) {
                throw new RateLimitExceeded(
                    "Hourly email limit reached, please try again later",
                    retryAfterSeconds
                );
            }

            // 4. registrar el envio: iniciar cooldown e incrementar el contador horario
            cooldownCache.put(key, Boolean.TRUE);
            window.increment();
        } catch (RateLimitExceeded ex) {
            throw ex;
        } catch (Exception ex) {
            // 5. cualquier fallo inesperado del limitador no debe bloquear el envio
            log.warn(
                "Email rate limiter failed unexpectedly, allowing send (fail-open): {}",
                ex.getMessage(),
                ex
            );
        }
    }

    // Contador de envios en una ventana fija de una hora desde su primera escritura.
    private static final class HourlyWindow {

        private final Instant windowStart = Instant.now();
        private final AtomicInteger count = new AtomicInteger(0);

        int retryAfterSecondsIfLimitReached(int maxPerHour) {
            if (count.get() < maxPerHour) {
                return 0;
            }
            Instant windowEnd = windowStart.plus(Duration.ofHours(1));
            long secondsLeft = Duration.between(
                Instant.now(),
                windowEnd
            ).getSeconds();
            return (int) Math.max(secondsLeft, 1);
        }

        void increment() {
            count.incrementAndGet();
        }
    }
}
