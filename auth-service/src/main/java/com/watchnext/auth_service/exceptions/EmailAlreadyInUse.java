package com.watchnext.auth_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyInUse extends WatchNextException {

    public EmailAlreadyInUse(String email) {
        super(
            HttpStatus.CONFLICT,
            "El email " + email + " ya está registrado."
        );
    }
}
