package com.watchnext.gateway.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends WatchNextException {

    public TokenExpiredException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
