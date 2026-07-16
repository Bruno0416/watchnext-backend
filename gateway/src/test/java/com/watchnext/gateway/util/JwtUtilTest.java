package com.watchnext.gateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtUtilTest {

    private static final String SECRET = "this-is-a-secret-key-that-is-long-enough-for-hmac-sha-512-algorithm";
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String EMAIL = "user@example.com";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", SECRET);
        jwtUtil.init();
    }

    @Test
    void extractUserId_returnsClaimIdNotSubject() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
            .subject(EMAIL)
            .claim("id", USER_ID)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 60_000))
            .signWith(key, Jwts.SIG.HS512)
            .compact();

        String result = jwtUtil.extractUserId(token);

        assertEquals(USER_ID, result);
    }
}
