package com.watchnext.auth_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class UnsupportedProvider extends WatchNextException {

    public UnsupportedProvider(String provider) {
        super(
            HttpStatus.BAD_REQUEST,
            "Proveedor de autenticacion no soportado: " + provider
        );
    }
}
