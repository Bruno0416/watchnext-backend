package com.watchnext.common.security.internal;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ServiceTokenProvider {

    private static final String CACHE_KEY = "current";

    private final SecretKey key;
    private final Duration expiration;
    private final String serviceName;
    private final Cache<String, String> tokenCache;

    public ServiceTokenProvider(ServiceTokenProperties props, String serviceName) {
        this.key = new SecretKeySpec(
            props.getSecretKey().getBytes(StandardCharsets.UTF_8),
            "HmacSHA512"
        );
        this.expiration = props.getExpiration();
        this.serviceName = serviceName;
        // 1. cache con ttl ligeramente menor a la expiracion para refresh proactivo
        this.tokenCache = Caffeine.newBuilder()
            .expireAfterWrite(props.getCacheTtl().toMillis(), TimeUnit.MILLISECONDS)
            .maximumSize(1)
            .build();
    }

    public String getToken() {
        // 1. devolver token cacheado o mintear uno nuevo
        return tokenCache.get(CACHE_KEY, k -> mintToken());
    }

    private String mintToken() {
        // 1. firmar jwt con claims minimos y expiracion corta
        return Jwts.builder()
            .subject(serviceName)
            .claim("token_type", "SERVICE")
            .expiration(new Date(System.currentTimeMillis() + expiration.toMillis()))
            .signWith(key)
            .compact();
    }
}
