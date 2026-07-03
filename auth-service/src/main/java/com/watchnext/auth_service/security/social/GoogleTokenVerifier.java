package com.watchnext.auth_service.security.social;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.watchnext.auth_service.config.GoogleOAuthProperties;
import com.watchnext.auth_service.dto.social.SocialUserInfo;
import com.watchnext.auth_service.enums.AuthProvider;
import com.watchnext.auth_service.exceptions.InvalidSocialToken;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.security.GeneralSecurityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleTokenVerifier implements SocialTokenVerifier {

    private final GoogleOAuthProperties googleOAuthProperties;

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    public void init() {
        // 1. construye el verificador con las audiencias permitidas (web client id)
        this.verifier = new GoogleIdTokenVerifier.Builder(
            new NetHttpTransport(),
            GsonFactory.getDefaultInstance()
        )
            .setAudience(googleOAuthProperties.getAllowedAudiences())
            .build();
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.GOOGLE;
    }

    @Override
    public SocialUserInfo verify(String token) {
        // 1. verifica firma, expiracion, issuer y audiencia del id_token
        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(token);
        } catch (GeneralSecurityException | IOException e) {
            throw new InvalidSocialToken(
                "No se pudo verificar el token de Google"
            );
        }

        if (idToken == null) {
            throw new InvalidSocialToken("Token de Google invalido o expirado");
        }

        // 2. normaliza el payload verificado a SocialUserInfo
        GoogleIdToken.Payload payload = idToken.getPayload();

        return new SocialUserInfo(
            AuthProvider.GOOGLE,
            payload.getSubject(),
            payload.getEmail(),
            Boolean.TRUE.equals(payload.getEmailVerified()),
            (String) payload.get("name"),
            (String) payload.get("picture")
        );
    }
}
