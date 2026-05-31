package com.watchnext.common.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WatchNextException extends RuntimeException {

    private final HttpStatus status;

    public WatchNextException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
