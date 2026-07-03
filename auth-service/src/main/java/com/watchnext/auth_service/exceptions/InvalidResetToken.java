package com.watchnext.auth_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class InvalidResetToken extends WatchNextException {

    public InvalidResetToken() {
        super(HttpStatus.BAD_REQUEST, "ResetToken Invalido o Expirado");
    }
}
