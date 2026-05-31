package com.watchnext.gateway.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class TokenInvalidException extends WatchNextException {

    public TokenInvalidException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
