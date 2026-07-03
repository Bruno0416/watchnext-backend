package com.watchnext.auth_service.security;

import com.watchnext.auth_service.entity.Users;
import com.watchnext.auth_service.exceptions.InvalidResetToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${application.security.jwt.secret-key}")
    private String jwtSecret;

    @Value("${application.security.jwt.access-token.expiration}")
    private long jwtExpirationMs;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpirationMs;

    // crea una variable privada para almacenar la SecretKey
    private SecretKey key;

    // extrae la SecretKey y la almacena en 'key'
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(
            jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
    }

    // ------------ Generar tokens ------------

    public String generateRefreshToken(Users user) {
        // El refresh token no necesita extraClaims (roles, name, etc.)
        return Jwts.builder()
            .subject(user.getUsername()) // Usamos el email como subject
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(
                new Date(System.currentTimeMillis() + refreshExpirationMs)
            )
            .signWith(key, Jwts.SIG.HS512)
            .compact();
    }

    // generar token publico llama al getToken privado
    public String generateToken(Users user) {
        // agregar extra claims (datos del usuario) para no requerir un endpoint extra
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", user.getId().toString());
        extraClaims.put("name", user.getEmail());
        extraClaims.put("email", user.getEmail());
        extraClaims.put("role", user.getRole().name());
        /*
            Guardamos los datos del usuario en el token usando los extraClaims
            para poder extraerlos en el resto de microservicios sin tener que comunicarnos directamente con Auth.
        */
        return getToken(extraClaims, user);
    }

    // ------------ tokens password recovery  ------------
    public String generateResetToken(String email) {
        return Jwts.builder()
            .subject(email)
            .claim("purpose", "PASSWORD_RESET")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000)) // 10 min
            .signWith(key, Jwts.SIG.HS512)
            .compact();
    }

    public String validateResetTokenAndGetEmail(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        if (!"PASSWORD_RESET".equals(claims.get("purpose", String.class))) {
            throw new InvalidResetToken();
        }
        return claims.getSubject();
    }

    // -----------------------------------------------------------

    // getToken genera el token real con claims(role y otros datos) y usuario
    private String getToken(Map<String, Object> extraClaims, UserDetails user) {
        return Jwts.builder()
            .claims(extraClaims)
            .subject(user.getUsername())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(key, Jwts.SIG.HS512)
            .compact();
    }

    // get username del token
    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    // token validator
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = getUsernameFromToken(token);
        return (
            username.equals(userDetails.getUsername()) && !isTokenExpired(token)
        );
    }

    // get claims
    public Claims getAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    // get claim (extrae un claim en especifico)
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // obtiene la fecha de expiracion
    public Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    // valida si el token esta expirado
    public boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }
}
