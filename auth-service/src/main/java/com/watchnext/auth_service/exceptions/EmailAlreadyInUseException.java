package com.watchnext.auth_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyInUseException extends WatchNextException {

    public EmailAlreadyInUseException(String email) {
        super(
            HttpStatus.CONFLICT,
            "El email " + email + " ya está registrado."
        );
    }
}
