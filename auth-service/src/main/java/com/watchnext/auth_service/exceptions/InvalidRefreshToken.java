package com.watchnext.auth_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class InvalidRefreshToken extends WatchNextException {

    public InvalidRefreshToken(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
