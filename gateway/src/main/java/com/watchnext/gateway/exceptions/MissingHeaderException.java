package com.watchnext.gateway.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class MissingHeaderException extends WatchNextException {

    public MissingHeaderException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
