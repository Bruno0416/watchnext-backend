package com.watchnext.gateway.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class TokenFormatInvalidException extends WatchNextException {

    public TokenFormatInvalidException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
