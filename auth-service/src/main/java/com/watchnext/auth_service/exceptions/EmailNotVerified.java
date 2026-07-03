package com.watchnext.auth_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class EmailNotVerified extends WatchNextException {

    public EmailNotVerified(String email) {
        super(
            HttpStatus.CONFLICT,
            "El email " +
                email +
                " ya esta registrado pero no esta verificado por el proveedor."
        );
    }
}
