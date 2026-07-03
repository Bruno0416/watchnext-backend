package com.watchnext.auth_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class SocialOnlyAccount extends WatchNextException {

    public SocialOnlyAccount() {
        super(
            HttpStatus.CONFLICT,
            "Esta cuenta usa inicio de sesion social. Continua con Google para acceder."
        );
    }
}
