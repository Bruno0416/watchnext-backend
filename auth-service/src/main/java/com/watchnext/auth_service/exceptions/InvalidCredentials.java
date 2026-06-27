package com.watchnext.auth_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class InvalidCredentials extends WatchNextException {

    public InvalidCredentials(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
